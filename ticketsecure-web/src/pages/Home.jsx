import { Ticket } from "lucide-react";
import  Chatbot  from "../components/Chatbot";

export default function Home() {
    return (
        <div className="min-h-screen bg-gray-50">
            {/* Barra de navegação Superior (NavBar)*/}
            <nav className="bg-blue-900 text-white p-4 shadow-md">
                <div className="max-w-6xl mx-auto flex items-center gap-2 font-bold text-2xl">
                    <Ticket className="w-8 h-8 text-yellow-400" />
                    TicketSecure
                </div>
            </nav>

            {/* Conteúdo Principal (IA)*/}
            <main className="max-w-6xl mx-aouto p-6 mt-8">
                <h1 className="text-4xl font-extrabold text-gray-800 tracking-tight">
                    Descubra eventos incríveis. <br />
                    <span className="text-blue-600">Com total segurança</span> 
                </h1>
                <p className="text-gray-500 mt-4 textlg max-w-2xl">
                    Converse com a nossa Inteligência Artificial para encontrar os melhores shows, ou navegue pelo nosso catálogo protegido pelo Cerebro AntiFraude.
                </p>

                <div className="mt-10">
                    <Chatbot />
                </div>
            </main>
        </div>
    );
}