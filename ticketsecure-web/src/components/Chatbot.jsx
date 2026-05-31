import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Loader2, MapPin, Ticket } from 'lucide-react';
import { api } from '../services/api';
import ReactMarkdown from 'react-markdown';

const CATEGORY_LABELS = {
    SHOW: 'Show',
    CINEMA: 'Cinema',
    THEATER: 'Teatro',
    FESTIVAL: 'Festival',
    OTHER: 'Evento',
};

function formatPrice(value) {
    return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatDate(iso) {
    return new Date(iso).toLocaleString('pt-BR', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit',
    });
}

function SuggestionCard({ item, onSelect }) {
    return (
        <button
            type="button"
            onClick={() => onSelect(item)}
            className="w-full text-left p-3 rounded-xl border border-blue-100 bg-white hover:border-blue-400 hover:shadow-md transition-all"
        >
            <div className="flex items-start justify-between gap-2">
                <div>
                    <p className="font-semibold text-gray-800 text-sm">{item.title}</p>
                    <p className="text-xs text-blue-600 mt-0.5">
                        {CATEGORY_LABELS[item.category] ?? item.category} · {item.city}
                    </p>
                </div>
                <span className="text-sm font-bold text-green-700 shrink-0">
                    {formatPrice(item.priceFrom)}
                </span>
            </div>
            <p className="text-xs text-gray-500 mt-1">{item.venue} · {formatDate(item.eventDate)}</p>
            <div className="flex gap-3 mt-2 text-xs text-gray-400">
                <span>{item.availableTickets} disponíveis</span>
                {item.distanceKm != null && (
                    <span>{item.distanceKm.toFixed(1)} km</span>
                )}
            </div>
        </button>
    );
}

export default function Chatbot() {
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [userLocation, setUserLocation] = useState(null);
    const [locationLabel, setLocationLabel] = useState(null);
    const [locationError, setLocationError] = useState(null);
    const [locating, setLocating] = useState(false);

    const messagesEndRef = useRef(null);

    const [messages, setMessages] = useState([
        {
            role: 'assistant',
            text: 'Olá! Sou a IA do TicketSecure. Me diga o que procura — show, cinema, teatro — e posso sugerir as melhores opções por **preço** e **distância**.',
            suggestions: [],
        },
    ]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, isLoading]);

    const requestLocation = async () => {
        if (!navigator.geolocation) {
            setLocationError('Seu navegador não suporta geolocalização. Digite a cidade no chat, ex.: "show em Campinas".');
            return;
        }
        setLocating(true);
        setLocationError(null);
        navigator.geolocation.getCurrentPosition(
            async (pos) => {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;
                setUserLocation({ lat, lng });
                try {
                    const { data } = await api.get('/geo/reverse', { params: { lat, lng } });
                    setLocationLabel(data.displayName ?? `${lat.toFixed(4)}, ${lng.toFixed(4)}`);
                } catch {
                    setLocationLabel(`${lat.toFixed(4)}, ${lng.toFixed(4)}`);
                }
                setLocating(false);
            },
            () => {
                setLocating(false);
                setLocationError('Não foi possível obter o GPS. Digite a cidade manualmente, ex.: "cinema barato em São Paulo".');
            },
            { timeout: 10000, enableHighAccuracy: false }
        );
    };

    const handleSuggestionClick = (item) => {
        setMessages((prev) => [
            ...prev,
            {
                role: 'assistant',
                text: `Ótima escolha! **${item.title}** a partir de ${formatPrice(item.priceFrom)}.\n\nPara reservar, use o fluxo de compra com o lote \`${item.lotId}\` ou peça: *"quero reservar ${item.title}"*.`,
                suggestions: [],
            },
        ]);
    };

    const handleSend = async (e) => {
        e.preventDefault();
        if (!input.trim() || isLoading) return;

        const userText = input.trim();
        setInput('');
        setMessages((prev) => [...prev, { role: 'user', text: userText }]);
        setIsLoading(true);

        try {
            const payload = { mensagem: userText };
            if (userLocation) {
                payload.latitude = userLocation.lat;
                payload.longitude = userLocation.lng;
            }

            const response = await api.post('/chat', payload);
            const assistantReply = response.data?.reply
                ?? 'Desculpe, não consegui processar sua resposta.';
            const suggestions = response.data?.suggestions ?? [];

            if (response.data?.locationLabel) {
                setLocationLabel(response.data.locationLabel);
            }

            setMessages((prev) => [
                ...prev,
                { role: 'assistant', text: assistantReply, suggestions },
            ]);
        } catch (error) {
            console.error('Erro ao conectar com a IA:', error);
            setMessages((prev) => [
                ...prev,
                {
                    role: 'assistant',
                    text: 'Desculpe, nossos servidores estão sobrecarregados. Tente novamente em instantes.',
                    suggestions: [],
                },
            ]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-2xl bg-white rounded-2xl shadow-xl flex flex-col border border-gray-100 max-h-[600px]">
            <div className="bg-blue-600 p-4 text-white flex items-center justify-between rounded-t-2xl">
                <div className="flex items-center gap-3">
                    <Bot className="w-6 h-6" />
                    <h3 className="font-bold text-lg">Assistente Inteligente</h3>
                </div>
                <button
                    type="button"
                    onClick={requestLocation}
                    disabled={locating}
                    title="Usar minha localização para sugestões por distância"
                    className="flex items-center gap-1 text-xs bg-blue-500 hover:bg-blue-400 px-2 py-1 rounded-full disabled:opacity-60"
                >
                    <MapPin className="w-3.5 h-3.5" />
                    {userLocation
                        ? (locationLabel ? locationLabel.split(',')[0] : 'GPS ativo')
                        : locating ? 'Localizando...' : 'Perto de mim'}
                </button>
            </div>
            {locationError && (
                <p className="text-xs text-amber-700 bg-amber-50 px-4 py-2 border-b border-amber-100">{locationError}</p>
            )}

            <div className="flex-1 p-4 overflow-y-auto min-h-0 bg-gray-50 flex flex-col gap-4">
                {messages.map((msg, index) => (
                    <div key={index}>
                        <div className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                            {msg.role === 'assistant' && (
                                <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 shrink-0">
                                    <Bot className="w-5 h-5" />
                                </div>
                            )}
                            <div className={`p-3 max-w-[85%] rounded-2xl text-sm shadow-sm
                                ${msg.role === 'user'
                                    ? 'bg-blue-600 text-white rounded-br-none'
                                    : 'bg-white text-gray-800 rounded-bl-none border border-gray-100'}`}>
                                <ReactMarkdown>{msg.text}</ReactMarkdown>
                            </div>
                            {msg.role === 'user' && (
                                <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-gray-600 shrink-0">
                                    <User className="w-5 h-5" />
                                </div>
                            )}
                        </div>

                        {msg.suggestions?.length > 0 && (
                            <div className="mt-2 ml-11 flex flex-col gap-2">
                                <p className="text-xs text-gray-500 flex items-center gap-1">
                                    <Ticket className="w-3 h-3" /> Sugestões do catálogo
                                </p>
                                {msg.suggestions.map((item) => (
                                    <SuggestionCard
                                        key={item.lotId}
                                        item={item}
                                        onSelect={handleSuggestionClick}
                                    />
                                ))}
                            </div>
                        )}
                    </div>
                ))}

                {isLoading && (
                    <div className="flex gap-3 justify-start">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 shrink-0">
                            <Loader2 className="w-5 h-5 animate-spin" />
                        </div>
                        <div className="p-3 bg-white text-gray-400 rounded-2xl rounded-bl-none border border-gray-100 text-sm shadow-sm">
                            Buscando no catálogo...
                        </div>
                    </div>
                )}

                <div ref={messagesEndRef} />
            </div>

            <form onSubmit={handleSend} className="p-4 bg-white border-t border-gray-100 flex gap-2">
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder={isLoading ? 'Aguardando...' : 'Ex: cinema barato perto de mim em São Paulo'}
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    disabled={isLoading}
                />
                <button
                    type="submit"
                    disabled={isLoading || !input.trim()}
                    className="bg-blue-600 text-white p-3 rounded-full hover:bg-blue-700 transition-colors disabled:bg-gray-300"
                >
                    <Send className="w-5 h-5" />
                </button>
            </form>
        </div>
    );
}
