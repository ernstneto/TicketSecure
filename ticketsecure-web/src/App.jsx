import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";


export default function App() {
  return (
    <BrowserRouter>
        <Routes>
            {/* Quando a URL for apenas "/", carregue a tela Home */}
            <Route path="/" element={<Home />} />
        </Routes>
    </BrowserRouter>
  );
}