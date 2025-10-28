import React, { useRef, useState } from 'react'

const Rag = () => {
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [ragAnswer, setRagAnswer] = useState('');
  const [file, setFile] = useState(null);
  const fileInputRef = useRef(null);

  const handleSubmitChat = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/chat/simple",{
          method: 'POST',
          headers: {
            'Content-Type':' application/json'
          },
          body: JSON.stringify({question})
        }
      );

      if (!response.ok) {
        throw new Error("Http Error Status : ", response.status);
      }

      const data = await response.text();
      setAnswer(data);
    } catch (error) {
      console.log("Error 기본 AI 요청 : ", error);  
      setAnswer("질문 처리 중 오류가 발생했습니다.");
    }
  }

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  }

  const handleUpload = async () => {
    if (!file) {
      alert("파일을 선택해 주세요");
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch("http://localhost:8080/api/documents/upload", {
          method: 'POST',
          body: formData
        }
      );

      if (!response.ok) {
        throw new Error("Http Error Status : ", response.status);
      }
    } catch (error) {
      console.log("파일 업로드 Error : ", error);
      alert("파일 업로드 중 오류 발생");
    }
  }

  const params = new URLSearchParams();
  params.append("question", question);

  const handleRagChat = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/documents/rag", {
          method: 'POST', 
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: params.toString()
        }
      );
      
      if (!response.ok) {
        throw new Error("Http Error : ", response.status);
      }

      const data = await response.text();
      setRagAnswer(data);
    } catch (error) {
      console.log("Error Fetching RAG Response : ", error);
      setRagAnswer("질문 처리 중 오류가 발생했습니다.")
    }
  }

  return (
    <div 
      style={{padding: '20px'}}
    >
      <h1>AI Chatbot With RAG</h1>
      <hr />

      <h2>PDF 업로드 (RAG 학습)</h2>
      <input type='file' onChange={handleFileChange} ref={fileInputRef} />
      <button onClick={handleUpload}>업로드 및 임베딩</button>
      <hr style={{margin: '20px 0px'}} />

      <h2>AI에게 질문하기</h2>
      <input 
        type="text" 
        onChange={(e) => setQuestion(e.target.value)}
        placeholder='질문을 입력해 주세요...'
        style={{width: '400px', marginRight: '10px'}}
      />
      <br /><br />

      <button style={{marginRight: '20px'}} onClick={handleSubmitChat}>기본 질문 (RAG 없음)</button>
      <button onClick={handleRagChat}>RAG 질문 (PDF 기반)</button>

      {answer && (
        <div style={{marginTop: '20px', backgroundColor: '#f0f0f0', padding: '10px'}}>
          <h3>기본 답변:</h3>
          <p>{answer}</p>
        </div>
      )}

      {ragAnswer && (
        <div style={{marginTop: '20px', backgroundColor: '#3546b7ff', padding: '10px'}}>
          <h3>RAG 답변:</h3>
          <p>{ragAnswer}</p>
        </div>
      )}
    </div>
  )
}

export default Rag