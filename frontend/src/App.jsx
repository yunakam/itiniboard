import { Navigate, Route, Routes } from "react-router-dom";
import PlanListPage from "./pages/PlanListPage";

function App() {
  return (
    <Routes>
      <Route path="/plans" element={<PlanListPage />} />
      <Route path="/" element={<Navigate to="/plans" replace />} />
      <Route path="*" element={<Navigate to="/plans" replace />} />
    </Routes>
  );
}

export default App;
