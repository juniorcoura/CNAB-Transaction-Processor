import axios from 'axios';
import { useEffect, useState } from 'react';

function App() {
  const [transactions, setTransactions] = useState([]);
  const [file, setFile] = useState(null);
  
  const fetchTransactions = async () => {
    const response = await axios.get("http://localhost:8080/transacoes")
    setTransactions(response.data);
  }

  const handleFileChange = (e) => {
    setFile(e.target.files[0])
  }

  const uploadFile = async () => {
    const formData = new FormData();
    formData.append('file', file);
    axios.post("http://localhost:8080/cnab/upload", formData, {
      headers: {
        'content-Type': 'multipart/form-data'
      } 
    });
  }

  const formatCurrency = (value) => {
    const formattedValue = new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency:'BRL',
    }).format(parseFloat(value));
    return formattedValue;
  }

  useEffect(() => {
    fetchTransactions();
  }, []  )

  return (
    <div className="p-8 font-sans bg-gray-50 min-h-screen">
      <h1 className='text-3xl font-bold mb-6 text-black'>Importação de CNAB</h1>
     <div className=" bg-white p-4 rounded-lg shadow-sm mb-8 flex items-center space-x-4">
        <label className="text-gray-600">
          <span className="bg-blue-500 hover:bg-blue-600 font-bold py-2 px-4 rounded
            cursor-point text-white"
          >
            Choose File
          </span>
          <input type="file" 
            className=""
            placeholder=""
            accept=".txt"
            onChange={handleFileChange}/>
        </label>
        <button onClick={uploadFile}
          className="bg-gray-600 text-white font-bold py-2 px-4 
          rounded hover-bg-gray-700 disabled:-bg-gray-300"
        >
          Upload File
        </button>
     </div>

     <div>
      <h2 className="text-3xl font-bold mb-6 text-black">Transações</h2>
      <ul className=" space-y-10">
        {transactions.map((report, reportIndex) => (
        <li key={reportIndex} className='"overflow-x-auto bg-white rounded-lg shadow-sm'>
          <div className="flex justify-between items-center mb-2">
            <h2 className="text-lg font-bold ml-4 mt-2">
              {report.nomeDaLoja}
            </h2>
            <span className={`text-md mr-4 mt-2 ${report.total>=0 ? 'text-green-700' : 'text-red-700'}`}>
              Total: {formatCurrency(report.total)}
            </span>
          </div>
          <table className ="min-w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Cartão</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">CPF</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Data</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Dono da Loja</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Hora</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Nome da Loja</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Tipo</th>
                <th className="px-4 py-2 text-left text-xs font-bold text-black tracking-wider">Valor</th>
              </tr>
            </thead>
              <tbody>
                {report.transacoes.map((transaction, index) => (
                <tr key={index} className="even:bg-gray-100">
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.cartao}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.cpf}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.data}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.donoDaLoja}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.hora}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.nomeDaLoja}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{transaction.tipo}</td>
                  <td className="px-4 py-2 border-t border-gray-200">{formatCurrency(transaction.valor)}</td>
                </tr>
                ))}
              </tbody>
          </table>
        </li>
        ))}
      </ul>
     </div>
    </div>
  )
}

export default App