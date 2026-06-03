import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Navbar } from "@/components/Navbar";
import DeviceListPage from "./pages/DeviceListPage";
import DeviceDetailPage from "./pages/DeviceDetailPage";
import RegisterDevicePage from "./pages/RegisterDevicePage";

function App() {
  return (
    <BrowserRouter>
      <div className="flex min-h-screen flex-col bg-background text-foreground">
        <Navbar />
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<DeviceListPage />} />
            <Route path="/devices" element={<DeviceListPage />} />
            <Route path="/devices/register" element={<RegisterDevicePage />} />
            <Route path="/devices/:id" element={<DeviceDetailPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
