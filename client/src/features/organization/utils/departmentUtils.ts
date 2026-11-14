import { DepartmentItem } from '../types/organization.types';

/**
 * Thuật toán tìm tất cả các phòng ban con cháu (trực tiếp và gián tiếp) của một phòng ban.
 * Dùng BFS duyệt đồ thị để đảm bảo tránh tạo chu trình (cycle detection).
 */
export function getDescendantDepartmentIds(
  departmentId: string,
  departments: DepartmentItem[]
): Set<string> {
  const descendants = new Set<string>();
  const queue = [departmentId];

  while (queue.length > 0) {
    const currentId = queue.shift()!;
    departments
      .filter((d) => d.parentId === currentId)
      .forEach((child) => {
        if (!descendants.has(child.id)) {
          descendants.add(child.id);
          queue.push(child.id);
        }
      });
  }

  return descendants;
}

/**
 * Kiểm tra xem việc gán proposedParentId làm phòng ban cha của departmentId có tạo chu trình hay không.
 * Điều kiện tạo chu trình:
 * 1. proposedParentId chính là departmentId (tự làm cha của mình).
 * 2. proposedParentId là một trong các phòng ban con/cháu của departmentId (chu trình gián tiếp: A -> B -> A).
 */
export function wouldCreateDepartmentCycle(
  departmentId: string,
  proposedParentId: string | null | undefined,
  departments: DepartmentItem[]
): boolean {
  if (!proposedParentId || proposedParentId.trim() === '') {
    return false; // Root department, không có cha -> không chu trình
  }

  if (proposedParentId === departmentId) {
    return true; // Tự tham chiếu
  }

  const descendants = getDescendantDepartmentIds(departmentId, departments);
  return descendants.has(proposedParentId);
}

export interface DepartmentTreeNode extends DepartmentItem {
  children: DepartmentTreeNode[];
  level: number;
}

/**
 * Chuyển đổi danh sách phòng ban phẳng thành cây phân cấp đa cấp để hiển thị UI
 */
export function buildDepartmentTree(departments: DepartmentItem[]): DepartmentTreeNode[] {
  const deptMap = new Map<string, DepartmentTreeNode>();

  departments.forEach((d) => {
    deptMap.set(d.id, { ...d, children: [], level: 0 });
  });

  const roots: DepartmentTreeNode[] = [];

  deptMap.forEach((node) => {
    if (node.parentId && deptMap.has(node.parentId)) {
      const parent = deptMap.get(node.parentId)!;
      node.level = parent.level + 1;
      parent.children.push(node);
    } else {
      node.level = 0;
      roots.push(node);
    }
  });

  return roots;
}
