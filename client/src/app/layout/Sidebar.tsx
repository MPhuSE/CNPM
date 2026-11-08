import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../providers/AuthProvider';
import {
  FileText,
  Inbox,
  LayoutDashboard,
  GitBranch,
  Users,
  ShieldAlert,
  History,
  Bell,
  PlusCircle,
} from 'lucide-react';
import { cn } from '@/shared/utils/cn';

interface NavItem {
  to: string;
  label: string;
  icon: React.ReactNode;
  role?: 'ROLE_EMPLOYEE' | 'ROLE_APPROVER' | 'ROLE_ADMIN';
  exact?: boolean;
}

export const Sidebar: React.FC = () => {
  const { hasRole } = useAuth();

  const employeeNav: NavItem[] = [
    { to: '/my-requests', label: 'Hồ sơ của tôi', icon: <FileText className="w-4 h-4" /> },
    { to: '/my-requests/new', label: 'Tạo hồ sơ mới', icon: <PlusCircle className="w-4 h-4" /> },
  ];

  const approverNav: NavItem[] = [
    { to: '/inbox', label: 'Hộp thư duyệt', icon: <Inbox className="w-4 h-4" /> },
  ];

  const adminNav: NavItem[] = [
    { to: '/dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
    { to: '/coordination', label: 'Điều phối & Sự cố', icon: <ShieldAlert className="w-4 h-4" /> },
    { to: '/workflows', label: 'Cấu hình quy trình', icon: <GitBranch className="w-4 h-4" /> },
    { to: '/organization', label: 'Tài khoản & Nhóm', icon: <Users className="w-4 h-4" /> },
    { to: '/audit', label: 'Nhật ký kiểm toán', icon: <History className="w-4 h-4" /> },
  ];

  const renderSection = (title: string, items: NavItem[]) => (
    <div className="mb-6">
      <div className="text-xs font-semibold uppercase text-gray-400 tracking-wider px-3 mb-2">
        {title}
      </div>
      <nav className="space-y-1">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors',
                isActive
                  ? 'bg-indigo-50 text-indigo-700 font-semibold'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              )
            }
          >
            {item.icon}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );

  return (
    <aside className="w-64 bg-white border-r border-gray-200 min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between shrink-0">
      <div>
        {hasRole('ROLE_EMPLOYEE') && renderSection('Nhân viên', employeeNav)}
        {hasRole('ROLE_APPROVER') && renderSection('Người phê duyệt', approverNav)}
        {hasRole('ROLE_ADMIN') && renderSection('Quản trị hệ thống', adminNav)}

        {/* Common general section */}
        <div className="mb-6">
          <div className="text-xs font-semibold uppercase text-gray-400 tracking-wider px-3 mb-2">
            Chung
          </div>
          <NavLink
            to="/notifications"
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors',
                isActive
                  ? 'bg-indigo-50 text-indigo-700 font-semibold'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              )
            }
          >
            <Bell className="w-4 h-4" />
            <span>Thông báo</span>
          </NavLink>
        </div>
      </div>

      <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 text-xs text-gray-500 text-center">
        FlowOps Engine 2.0<br />
        <span className="text-[10px] text-gray-400">Modular Monolith Arch</span>
      </div>
    </aside>
  );
};
