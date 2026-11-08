import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Card } from '@/shared/ui/Card';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { Users, Building, Shield } from 'lucide-react';

export const OrganizationPage: React.FC = () => {
  const [tab, setTab] = useState<'users' | 'departments' | 'groups'>('users');

  const { data: users, isLoading: loadingUsers } = useQuery({
    queryKey: ['org-users'],
    queryFn: () => apiClient<any>(ENDPOINTS.USERS),
    enabled: tab === 'users',
  });

  const { data: departments, isLoading: loadingDepts } = useQuery({
    queryKey: ['org-departments'],
    queryFn: () => apiClient<any>(ENDPOINTS.DEPARTMENTS),
    enabled: tab === 'departments',
  });

  const { data: groups, isLoading: loadingGroups } = useQuery({
    queryKey: ['org-groups'],
    queryFn: () => apiClient<any>(ENDPOINTS.APPROVAL_GROUPS),
    enabled: tab === 'groups',
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Quản trị Tổ chức &amp; Phân quyền</h1>
        <p className="text-sm text-gray-500 mt-1">
          Quản lý tài khoản, phòng ban và các nhóm phê duyệt độc lập (Finance, Accounting, IT)
        </p>
      </div>

      <div className="flex border-b border-gray-200">
        <button
          onClick={() => setTab('users')}
          className={`flex items-center gap-2 py-3 px-6 text-sm font-semibold border-b-2 transition-colors ${
            tab === 'users'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Users className="w-4 h-4" />
          Người dùng &amp; Vai trò
        </button>

        <button
          onClick={() => setTab('departments')}
          className={`flex items-center gap-2 py-3 px-6 text-sm font-semibold border-b-2 transition-colors ${
            tab === 'departments'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Building className="w-4 h-4" />
          Phòng ban
        </button>

        <button
          onClick={() => setTab('groups')}
          className={`flex items-center gap-2 py-3 px-6 text-sm font-semibold border-b-2 transition-colors ${
            tab === 'groups'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Shield className="w-4 h-4" />
          Nhóm phê duyệt
        </button>
      </div>

      <Card>
        {tab === 'users' && (
          loadingUsers ? (
            <LoadingSpinner label="Đang tải danh sách người dùng..." />
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>Tài khoản</TableHeaderCell>
                  <TableHeaderCell>Họ và tên</TableHeaderCell>
                  <TableHeaderCell>Email</TableHeaderCell>
                  <TableHeaderCell>Phòng ban</TableHeaderCell>
                  <TableHeaderCell>Vai trò (Roles)</TableHeaderCell>
                  <TableHeaderCell>Trạng thái</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users?.items?.map((u: any) => (
                  <TableRow key={u.id}>
                    <TableCell className="font-mono text-xs font-semibold text-gray-800">
                      @{u.username}
                    </TableCell>
                    <TableCell className="font-medium text-gray-900">{u.fullName}</TableCell>
                    <TableCell className="text-xs text-gray-600">{u.email}</TableCell>
                    <TableCell className="text-xs">{u.departmentName || '—'}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map((r: string) => (
                          <Badge
                            key={r}
                            size="sm"
                            color={r === 'ROLE_ADMIN' ? 'rose' : r === 'ROLE_APPROVER' ? 'indigo' : 'blue'}
                          >
                            {r.replace('ROLE_', '')}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge color={u.isActive ? 'emerald' : 'gray'}>
                        {u.isActive ? 'Active' : 'Locked'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )
        )}

        {tab === 'departments' && (
          loadingDepts ? (
            <LoadingSpinner label="Đang tải danh sách phòng ban..." />
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>Mã phòng ban</TableHeaderCell>
                  <TableHeaderCell>Tên phòng ban</TableHeaderCell>
                  <TableHeaderCell>Trưởng bộ phận</TableHeaderCell>
                  <TableHeaderCell>Trạng thái</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {departments?.items?.map((d: any) => (
                  <TableRow key={d.id}>
                    <TableCell className="font-mono text-xs font-semibold">{d.code}</TableCell>
                    <TableCell className="font-medium text-gray-900">{d.name}</TableCell>
                    <TableCell className="text-xs text-gray-700">{d.managerName || '—'}</TableCell>
                    <TableCell>
                      <Badge color={d.isActive ? 'emerald' : 'gray'}>
                        {d.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )
        )}

        {tab === 'groups' && (
          loadingGroups ? (
            <LoadingSpinner label="Đang tải danh sách nhóm duyệt..." />
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableHeaderCell>Mã nhóm</TableHeaderCell>
                  <TableHeaderCell>Tên nhóm</TableHeaderCell>
                  <TableHeaderCell>Số thành viên</TableHeaderCell>
                  <TableHeaderCell>Trạng thái</TableHeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {groups?.items?.map((g: any) => (
                  <TableRow key={g.id}>
                    <TableCell className="font-mono text-xs font-semibold">{g.code}</TableCell>
                    <TableCell className="font-medium text-gray-900">{g.name}</TableCell>
                    <TableCell className="text-xs">{g.memberCount || 0} thành viên</TableCell>
                    <TableCell>
                      <Badge color={g.isActive ? 'emerald' : 'gray'}>
                        {g.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )
        )}
      </Card>
    </div>
  );
};
