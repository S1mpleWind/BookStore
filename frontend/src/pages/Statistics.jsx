import React, { useState, useEffect } from 'react';
import { DatePicker, Table, Spin, Empty, Tabs } from 'antd';
import { useUser } from '../components/UserContext';
import { getSalesRanking, getConsumptionRanking, getMyStatistics } from '../api';

const { RangePicker } = DatePicker;

const Statistics = () => {
  const { user } = useUser();
  const isAdmin = user?.identity === 1;
  const [dateRange, setDateRange] = useState(null);
  const [salesData, setSalesData] = useState([]);
  const [consumptionData, setConsumptionData] = useState([]);
  const [myStats, setMyStats] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadData = () => {
    setLoading(true);
    const start = dateRange?.[0]?.toISOString();
    const end = dateRange?.[1]?.toISOString();
    const promises = [];
    if (isAdmin) {
      promises.push(getSalesRanking(start, end).then(setSalesData).catch(() => {}));
      promises.push(getConsumptionRanking(start, end).then(setConsumptionData).catch(() => {}));
    } else {
      promises.push(getMyStatistics(start, end).then(setMyStats).catch(() => {}));
    }
    Promise.all(promises).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, []);

  const salesColumns = [
    { title: '排名', key: 'index', render: (_, __, i) => i + 1 },
    { title: '书名', dataIndex: 'bookTitle', key: 'bookTitle' },
    { title: '销量', dataIndex: 'sales', key: 'sales' },
  ];

  const consumptionColumns = [
    { title: '排名', key: 'index', render: (_, __, i) => i + 1 },
    { title: '用户', dataIndex: 'nickname', key: 'nickname' },
    { title: '消费总额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v) => `¥${(v / 100).toFixed(2)}` },
  ];

  return (
    <div className="content-inner">
      <h1 className="page-title">{isAdmin ? '数据统计' : '我的统计'}</h1>
      <div style={{ marginBottom: 16 }}>
        <RangePicker showTime onChange={setDateRange} />
        <button onClick={loadData} style={{ marginLeft: 8, padding: '4px 16px' }}>查询</button>
      </div>
      {loading ? <Spin /> : (
        isAdmin ? (
          <Tabs items={[
            {
              key: 'sales',
              label: '热销榜',
              children: salesData.length === 0 ? <Empty /> : <Table rowKey="bookId" dataSource={salesData} columns={salesColumns} />,
            },
            {
              key: 'consumption',
              label: '消费榜',
              children: consumptionData.length === 0 ? <Empty /> : <Table rowKey="userId" dataSource={consumptionData} columns={consumptionColumns} />,
            },
          ]} />
        ) : (
          myStats ? (
            <div>
              <p><strong>购买总本数：</strong>{myStats.totalBooks} 本</p>
              <p><strong>购买总金额：</strong>¥{(myStats.totalAmount / 100).toFixed(2)}</p>
              <h3>购买明细</h3>
              <Table rowKey="bookId" dataSource={myStats.details || []} pagination={false}
                columns={[
                  { title: '书名', dataIndex: 'bookTitle' },
                  { title: '数量', dataIndex: 'quantity' },
                ]}
              />
            </div>
          ) : <Empty />
        )
      )}
    </div>
  );
};

export default Statistics;