import React, { useState } from 'react';
import { Button, Drawer } from 'antd';
import { Link } from 'react-router-dom';
import AssistantChat from './AssistantChat';

export default function AssistantWidget() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <Button className="assistant-float-button" type="primary" shape="circle" onClick={() => setOpen(true)} aria-label="打开智能助手">✦</Button>
      <Drawer title="智能书店助手" placement="right" width={420} open={open} onClose={() => setOpen(false)}>
        <AssistantChat compact />
        <Link to="/assistant" onClick={() => setOpen(false)}><Button block>打开完整助手页面</Button></Link>
      </Drawer>
    </>
  );
}
