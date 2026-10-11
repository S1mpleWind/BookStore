#!/usr/bin/env python3
"""对实际运行的后端进行 HTTP 验证，保存可截图的真实响应报告。仅使用标准库。"""
import argparse
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
import html
import json
from pathlib import Path
import time
from urllib.error import HTTPError
from urllib.request import Request, urlopen


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-url', default='http://localhost:8080')
    parser.add_argument('--username', default='demo')
    parser.add_argument('--password', default='demo123')
    parser.add_argument('--book-id', type=int, default=1)
    parser.add_argument('--requests', type=int, default=32)
    parser.add_argument('--workers', type=int, default=16)
    parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[1] / 'docs/evidence')
    args = parser.parse_args()
    if args.requests <= 0 or args.workers <= 0:
        parser.error('requests 和 workers 必须为正整数')
    records = []

    def request(path, token=None, body=None):
        headers = {}
        if token is not None:
            headers['Authorization'] = 'Bearer ' + token
        data = None
        if body is not None:
            headers['Content-Type'] = 'application/json'
            data = json.dumps(body).encode()
        req = Request(args.base_url.rstrip('/') + path, data=data, headers=headers)
        try:
            response = urlopen(req, timeout=30)
        except HTTPError as error:
            response = error
        with response:
            raw = response.read().decode()
            try:
                content = json.loads(raw)
            except ValueError:
                content = raw
            return {'url': req.full_url, 'method': req.get_method(),
                    'authorization': '(未携带)' if token is None else 'Bearer ' + token[:18] + '...（已截断）',
                    'status': response.status, 'headers': dict(response.headers), 'body': content}

    path = f'/api/v1/books/{args.book_id}'
    records.append(request(path))
    login = request('/api/v1/users/login', body={'username': args.username, 'password': args.password})
    assert login['status'] == 200, f'登录失败：{login["status"]}'
    token = login['body']['token']
    assert 'Set-Cookie' not in login['headers'], '登录响应不应该创建 Session Cookie'
    records.append(request(path, 'invalid-token'))
    records.append(request(path, token))
    assert [r['status'] for r in records] == [401, 401, 200], records
    assert records[-1]['headers'].get('X-Virtual-Thread') == 'true', '请求未使用虚拟线程'
    started = time.perf_counter()
    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        responses = list(executor.map(lambda _: request(path, token), range(args.requests)))
    elapsed = time.perf_counter() - started
    assert all(r['status'] == 200 and r['headers'].get('X-Virtual-Thread') == 'true' for r in responses)
    report = {'capturedAt': datetime.now(timezone.utc).isoformat(), 'records': records,
              'concurrency': {'requests': args.requests, 'workers': args.workers,
                              'successful': len(responses), 'elapsedSeconds': round(elapsed, 3)}}
    args.output.mkdir(parents=True, exist_ok=True)
    (args.output / 'jwt-verification.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    cards = []
    labels = ['无 Token：401', '错误 Token：401', '有效 Token：200']
    for label, record in zip(labels, records):
        cards.append(f'<section><h2>{label}</h2><pre>{html.escape(json.dumps(record, ensure_ascii=False, indent=2))}</pre></section>')
    document = '''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>JWT HTTP 验证记录</title>
<style>body{font:16px sans-serif;margin:32px;background:#f3f5f8;color:#172033}section{background:white;border:1px solid #ccd5e0;border-radius:8px;padding:16px;margin:16px 0}pre{white-space:pre-wrap;overflow-wrap:anywhere;font:14px monospace}h2{margin:0 0 12px}</style>
<h1>书店 JWT 与虚拟线程验证</h1><p>以下内容来自脚本对运行中后端发起的真实 HTTP 请求，Token 已截断。</p>'''
    document += f'<p>记录时间（UTC）：{html.escape(report["capturedAt"])}</p>' + ''.join(cards)
    document += '<section><h2>并发请求验证</h2><pre>' + html.escape(json.dumps(report['concurrency'], indent=2)) + '</pre><p>所有请求返回 200，且 X-Virtual-Thread: true。此结果用于功能验证，不是性能对比基准。</p></section></html>'
    (args.output / 'jwt-verification.html').write_text(document, encoding='utf-8')
    print('PASS: 无 Token 401；错误 Token 401；有效 Token 200；真实请求使用虚拟线程')
    print(f'并发 {args.requests} 个请求，客户端并发数 {args.workers}，耗时 {elapsed:.3f} 秒')
    print(f'证据目录：{args.output}')


if __name__ == '__main__':
    main()
