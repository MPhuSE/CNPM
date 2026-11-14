import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { organizationApi } from '../services/organizationApi';
import { UserList } from '../components/UserList';
import { DepartmentManager } from '../components/DepartmentManager';
import { ApprovalGroupEditor } from '../components/ApprovalGroupEditor';
import { Card } from '@/shared/ui/Card';
import { Users, Building, Shield, UserCheck, FolderTree } from 'lucide-react';

export const OrganizationPage: React.FC = () => {
  const [tab, setTab] = useState<'users' | 'departments' | 'groups'>('users');

  // Pre-fetch departments and users to supply to sub-components (for dropdowns, manager selection, etc.)
  const { data: deptsData } = useQuery({
    queryKey: ['departments-all-dropdown'],
    queryFn: () => organizationApi.getDepartments({ page: 0, size: 100 }),
  });

  const { data: usersData } = useQuery({
    queryKey: ['users-all-dropdown'],
    queryFn: () => organizationApi.getUsers({ page: 0, size: 100 }),
  });

  const departments = deptsData?.items || [];
  const users = usersData?.items || [];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">
              Quản trị Tài khoản &amp; Tổ chức
            </h1>
            <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-100">
              FR02–03 • Admin Only
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Quản trị người dùng, gán 3 vai trò hệ thống, cơ cấu phòng ban và quản lý các nhóm phê duyệt độc lập (Finance, Accounting, IT).
          </p>
        </div>

        {/* Quick summary stats */}
        <div className="flex items-center gap-3">
          <div className="bg-white px-3.5 py-2 rounded-xl border border-gray-200 shadow-sm flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[11px] text-gray-400 uppercase font-medium">Người dùng</div>
              <div className="text-base font-bold text-gray-900">{usersData?.total ?? users.length}</div>
            </div>
          </div>

          <div className="bg-white px-3.5 py-2 rounded-xl border border-gray-200 shadow-sm flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Building className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[11px] text-gray-400 uppercase font-medium">Phòng ban</div>
              <div className="text-base font-bold text-gray-900">{deptsData?.total ?? departments.length}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs navigation */}
      <div className="flex border-b border-gray-200 gap-2">
        <button
          onClick={() => setTab('users')}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-semibold border-b-2 transition-all ${
            tab === 'users'
              ? 'border-indigo-600 text-indigo-600 bg-indigo-50/30 rounded-t-lg'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          <span>Người dùng &amp; 3 Vai trò</span>
        </button>

        <button
          onClick={() => setTab('departments')}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-semibold border-b-2 transition-all ${
            tab === 'departments'
              ? 'border-indigo-600 text-indigo-600 bg-indigo-50/30 rounded-t-lg'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
        >
          <FolderTree className="w-4 h-4" />
          <span>Phòng ban &amp; Trưởng bộ phận</span>
        </button>

        <button
          onClick={() => setTab('groups')}
          className={`flex items-center gap-2 py-3 px-5 text-sm font-semibold border-b-2 transition-all ${
            tab === 'groups'
              ? 'border-indigo-600 text-indigo-600 bg-indigo-50/30 rounded-t-lg'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
          }`}
        >
          <Shield className="w-4 h-4" />
          <span>Nhóm phê duyệt độc lập</span>
        </button>
      </div>

      {/* Active Tab Content */}
      <div className="mt-4">
        {tab === 'users' && <UserList departments={departments} />}
        {tab === 'departments' && <DepartmentManager availableUsers={users} />}
        {tab === 'groups' && <ApprovalGroupEditor availableUsers={users} />}
      </div>
    </div>
  );
};
