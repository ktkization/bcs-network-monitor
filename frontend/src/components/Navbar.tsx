import { Link, useNavigate } from "react-router-dom";
import { Monitor, PlusCircle } from "lucide-react";
import { Button } from "@/components/ui/button";

export function Navbar() {
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <Link to="/devices" className="flex items-center gap-2 font-semibold tracking-tight">
          <Monitor className="h-5 w-5 text-primary" />
          <span>BCS Network Monitor</span>
        </Link>
      </div>
    </header>
  );
}
