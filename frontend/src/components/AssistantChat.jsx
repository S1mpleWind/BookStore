import React, { useState } from 'react';
import { Alert, Button, Input, Spin, Tag } from 'antd';
import { chatWithAssistant } from '../api';

const example = '帮我查一下 ISBN 为 978-7-5302-1015-3 的书还有多少本，顺便看看别家卖多少钱';

export default function AssistantChat({ compact = false }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [toolCalls, setToolCalls] = useState([]);

  const send = async (text = input) => {
    const content = text.trim();
    if (!content || loading) return;
    const next = [...messages, { role: 'user', content }];
    setMessages(next);
    setInput('');
    setError('');
    setLoading(true);
    try {
      const result = await chatWithAssistant(next);
      setMessages([...next, { role: 'assistant', content: result.reply }]);
      setToolCalls(result.toolCalls || []);
    } catch (err) {
      setError(err.message || '助手请求失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`assistant-chat ${compact ? 'assistant-chat-compact' : ''}`}>
      {!compact && (
        <div className="assistant-intro">
          <span className="assistant-kicker">AI CUSTOMER SUPPORT</span>
          <h1>智能书店助手</h1>
          <p>我可以查询本店实时库存，并模拟比较竞争对手价格。</p>
        </div>
      )}
      {messages.length === 0 && (
        <div className="assistant-example">
          <span>试试：</span>
          <Button type="link" onClick={() => send(example)}>{example}</Button>
        </div>
      )}
      <div className="assistant-messages" aria-live="polite">
        {messages.map((message, index) => (
          <div className={`assistant-message ${message.role}`} key={`${message.role}-${index}`}>
            <span className="assistant-message-role">{message.role === 'user' ? '你' : '助手'}</span>
            <div>{message.content}</div>
          </div>
        ))}
        {loading && <div className="assistant-loading"><Spin size="small" /> 正在查询库存和价格…</div>}
      </div>
      {toolCalls.length > 0 && (
        <section className="assistant-tools" aria-label="Function Calling 执行记录">
          <h3>Function Calling 执行记录</h3>
          {toolCalls.map((tool, index) => (
            <div className="assistant-tool-card" key={`${tool.name}-${index}`}>
              <div className="assistant-tool-head">
                <code>{tool.name}</code><Tag color={tool.status === 'completed' ? 'green' : 'red'}>{tool.status}</Tag>
              </div>
              <pre>{JSON.stringify({ arguments: tool.arguments, result: tool.result }, null, 2)}</pre>
            </div>
          ))}
        </section>
      )}
      {error && <Alert type="error" showIcon message="助手请求失败" description={error} />}
      <div className="assistant-composer">
        <Input.TextArea
          value={input}
          onChange={(event) => setInput(event.target.value)}
          onPressEnter={(event) => { if (!event.shiftKey) { event.preventDefault(); send(); } }}
          placeholder="例如：查询 ISBN 978-7-5302-1015-3 的库存和竞品价格"
          autoSize={{ minRows: 2, maxRows: 5 }}
          disabled={loading}
        />
        <Button type="primary" onClick={() => send()} loading={loading}>发送</Button>
      </div>
    </div>
  );
}
