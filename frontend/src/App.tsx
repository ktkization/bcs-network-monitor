import { BrowserRouter, Routes, Route } from "react-router-dom";
import DeviceListPage from "./pages/DeviceListPage";
import DeviceDetailPage from "./pages/DeviceDetailPage";
import RegisterDevicePage from "./pages/RegisterDevicePage";

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/" element={<DeviceListPage />} />
          <Route path="/devices" element={<DeviceListPage />} />
          <Route path="/devices/register" element={<RegisterDevicePage />} />
          <Route path="/devices/:id" element={<DeviceDetailPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
