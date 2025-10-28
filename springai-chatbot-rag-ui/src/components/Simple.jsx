import React, { useState } from 'react'

const Simple = () => {

  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');

  const handleSimpleChat = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/chat/simple', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({question})
      });

      console.log(response);

      if (!response.ok) {
        throw new Error(`HTTP Error! statue : ${response.status}`);
      }

      const data = await response.text();
      setAnswer(data);
      
    } catch (error) {
      console.log('Error fetching simple chat response', error);
      setAnswer('질문 처리 중 오류가 발생 했습니다.');
    }
  }

  return (
    <div>
      <h1>RAG 없는 기본 Gemini 호출</h1>
      <input 
        type="text" 
        value={question} 
        onChange={(e) => setQuestion(e.target.value)} 
        placeholder='질문을 입력하세요.....'
      />
      <button onClick={handleSimpleChat}>기본 질문하기</button>
      { answer && <div><strong>답변: </strong><p>{answer}</p></div> }
    </div>
  )
}

export default Simple