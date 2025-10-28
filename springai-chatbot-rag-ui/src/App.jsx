import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import { BrowserRouter, Routes, Route, Link} from 'react-router-dom'

import './App.css'
import Simple from './components/simple'
import Rag from './components/RAG'

function App() {
  const [count, setCount] = useState(0)

  return (
    <BrowserRouter>
      <nav 
        style={ 
          {
            margin: '20px',
            display: 'flex',
            gap: '10px',
            justifyContent: 'center',
            alignItems: 'center'
          } 
        }
      >
        <Link to="/simple">일반 호출</Link>
        <Link to="/rag">RAG 호출</Link>
      </nav>
      <Routes>
        <Route path='/simple' element={ <Simple /> } />
        <Route path='/rag' element={ <Rag /> } />
        <Route path='/' element={ <div>404 페이지 요청 없음</div> } />
      </Routes>
    </BrowserRouter>
  )
}

export default App
