import React from 'react';
import { useAuth } from '../providers/AuthProvider';
import { Bell, LogOut, User } from 'lucide-react';
import { Badge } from '@/shared/ui/Badge';
import { Link } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <Link to="/" className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-lg">
            F
          </div>
          <span className="font-bold text-xl text-gray-900 tracking-tight">FlowOps</span>
        </Link>
        <span className="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded">v2.0 MVP</span>
      </div>

      <div className="flex items-center gap-4">
        {/* Notification bell */}
        <Link
          to="/notifications"
          className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg relative transition-colors"
          title="Thông báo"
        >
          <Bell className="w-5 h-5" />
        </Link>

        {/* User profile dropdown info */}
        {user && (
          <div className="flex items-center gap-3 pl-3 border-l border-gray-200">
            <div className="flex flex-col text-right">
              <span className="text-sm font-semibold text-gray-900">{user.fullName}</span>
              <div className="flex items-center gap-1 justify-end mt-0.5">
                {user.roles.map((r) => (
                  <Badge key={r} size="sm" color={r === 'ROLE_ADMIN' ? 'rose' : r === 'ROLE_APPROVER' ? 'indigo' : 'blue'}>
                    {r.replace('ROLE_', '')}
                  </Badge>
                ))}
              </div>
            </div>

            <button
              onClick={logout}
              className="p-2 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors ml-1"
              title="Đăng xuất"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>
    </header>
  );
};
