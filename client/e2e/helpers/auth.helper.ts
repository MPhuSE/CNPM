import { Page } from '@playwright/test';
import { TEST_USERS } from '../fixtures/test-seed';

/**
 * Helper đăng nhập nhanh theo Role cho Playwright
 * @param page Playwright Page
 * @param role Tên role ('admin' | 'employee' | 'approver' | 'finance_bob')
 */
export async function loginAs(page: Page, role: 'admin' | 'employee' | 'approver' | 'finance_bob' = 'approver') {
  const user = TEST_USERS[role];
  if (!user) {
    throw new Error(`User with role ${role} not found in test fixtures.`);
  }

  // Nạp tài khoản kiểm thử vào localStorage trước khi trang web khởi tạo
  await page.addInitScript((userData) => {
    localStorage.setItem('flowops_user', JSON.stringify(userData));
    localStorage.setItem('flowops_token', 'mock_jwt_token_for_e2e_testing');
  }, user);

  await page.goto('/inbox');
  await page.waitForLoadState('networkidle');
}

/**
 * Quy ước lưu ảnh bằng chứng nghiệm thu
 * Playwright tự động tạo thư mục cha nếu chưa tồn tại.
 * @param page Playwright Page
 * @param ticketCode Mã ticket
 * @param caseName Tên ca kiểm thử
 * @param status Kết quả
 */
export async function captureEvidence(
  page: Page,
  ticketCode: string,
  caseName: string,
  status: 'PASS' | 'FAIL' = 'PASS'
): Promise<string> {
  const filePath = `e2e-evidence/${ticketCode}_${caseName}_${status}.png`;
  await page.screenshot({ path: filePath, fullPage: true });
  return filePath;
}
