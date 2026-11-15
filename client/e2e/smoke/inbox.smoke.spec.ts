import { test, expect } from '@playwright/test';
import { loginAs, captureEvidence } from '../helpers/auth.helper';

test.describe('@smoke @SCRUM-84 @SCRUM-15 Hộp thư duyệt (Task Inbox) & Chi tiết nhiệm vụ', () => {

  test.beforeEach(async ({ page }) => {
    await test.step('Chuẩn bị: Đăng nhập hệ thống với quyền Người duyệt (Approver)', async () => {
      await loginAs(page, 'approver');
    });
  });

  test('[SMOKE-01] Hiển thị Hộp thư duyệt với 2 tab Cá nhân và Nhóm (FR13, AT03)', async ({ page }) => {
    await test.step('Bước 1: Điều hướng vào trang Hộp thư duyệt (/inbox)', async () => {
      await page.goto('/inbox');
      await expect(page).toHaveURL(/.*\/inbox/);
    });

    await test.step('Bước 2: Kiểm tra tiêu đề trang "Hộp thư duyệt"', async () => {
      const pageTitle = page.locator('h1');
      await expect(pageTitle).toContainText('Hộp thư duyệt');
    });

    await test.step('Bước 3: Xác minh sự hiện diện của 2 tab Cá nhân và Nhóm', async () => {
      const mineTab = page.getByRole('button', { name: /Nhiệm vụ của tôi/i });
      const groupTab = page.getByRole('button', { name: /Nhiệm vụ nhóm/i });
      await expect(mineTab).toBeVisible();
      await expect(groupTab).toBeVisible();
    });

    await test.step('Bước 4: Kiểm tra bảng danh sách nhiệm vụ hiển thị đầy đủ', async () => {
      const table = page.locator('table');
      await expect(table).toBeVisible();
    });

    await test.step('Bước 5: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '01_inbox_tabs_view', 'PASS');
    });
  });

  test('[SMOKE-02] Chuyển đổi sang tab Nhiệm vụ nhóm và kiểm tra trạng thái GROUP_OPEN (FR13, FR15)', async ({ page }) => {
    await test.step('Bước 1: Mở trang Hộp thư duyệt', async () => {
      await page.goto('/inbox');
    });

    await test.step('Bước 2: Bấm chuyển sang tab "Nhiệm vụ nhóm (Cần nhận việc)"', async () => {
      const groupTab = page.getByRole('button', { name: /Nhiệm vụ nhóm/i });
      await groupTab.click();
      await page.waitForTimeout(500);
    });

    await test.step('Bước 3: Xác minh xuất hiện Badge trạng thái "Chờ nhận việc"', async () => {
      const groupBadge = page.locator('text=Chờ nhận việc').first();
      await expect(groupBadge).toBeVisible();
    });

    await test.step('Bước 4: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '02_inbox_group_tab', 'PASS');
    });
  });

  test('[SMOKE-03] Xem chi tiết nhiệm vụ và snapshot biểu mẫu Revision (FR14, AT03)', async ({ page }) => {
    await test.step('Bước 1: Mở trang chi tiết nhiệm vụ (ID: 1)', async () => {
      await page.goto('/inbox/1');
      await page.waitForSelector('h1');
    });

    await test.step('Bước 2: Kiểm tra tiêu đề hồ sơ đề xuất', async () => {
      const detailHeader = page.locator('h1');
      await expect(detailHeader).toContainText('Đề xuất mua màn hình Dell UltraSharp');
    });

    await test.step('Bước 3: Kiểm tra hiển thị nội dung Revision Snapshot', async () => {
      const snapshotCard = page.locator('text=Nội dung hồ sơ cần xét duyệt (Revision snapshot)');
      await expect(snapshotCard).toBeVisible();
    });

    await test.step('Bước 4: Xác minh bộ 3 nút hành động (Chấp thuận, Yêu cầu bổ sung, Từ chối)', async () => {
      const approveBtn = page.getByRole('button', { name: /Chấp thuận/i });
      const changesBtn = page.getByRole('button', { name: /Yêu cầu bổ sung/i });
      const rejectBtn = page.getByRole('button', { name: /Từ chối/i });

      await expect(approveBtn).toBeVisible();
      await expect(changesBtn).toBeVisible();
      await expect(rejectBtn).toBeVisible();
    });

    await test.step('Bước 5: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '03_task_detail_snapshot', 'PASS');
    });
  });

  test('[SMOKE-04] Cảnh báo missing assignment khi nhiệm vụ ở trạng thái NEEDS_ASSIGNMENT (FR15, AT10)', async ({ page }) => {
    await test.step('Bước 1: Mở nhiệm vụ ở trạng thái Cần phân công (ID: 2)', async () => {
      await page.goto('/inbox/2');
    });

    await test.step('Bước 2: Kiểm tra hiển thị Alert cảnh báo màu vàng về phân công', async () => {
      const warningAlert = page.locator('text=Nhiệm vụ này đang ở trạng thái Cần phân công');
      await expect(warningAlert).toBeVisible();
    });

    await test.step('Bước 3: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '04_needs_assignment_alert', 'PASS');
    });
  });

  test('[SMOKE-05] Thao tác Nhận việc (Claim) trên nhiệm vụ nhóm mở (FR15, AT06)', async ({ page }) => {
    await test.step('Bước 1: Mở nhiệm vụ nhóm đang chờ nhận việc (ID: 4)', async () => {
      await page.goto('/inbox/4');
    });

    await test.step('Bước 2: Kiểm tra sự hiện diện của nút "Nhận việc (Claim)"', async () => {
      const claimButton = page.getByRole('button', { name: /Nhận việc \(Claim\)/i });
      await expect(claimButton).toBeVisible();
    });

    await test.step('Bước 3: Click nút "Nhận việc (Claim)"', async () => {
      const claimButton = page.getByRole('button', { name: /Nhận việc \(Claim\)/i });
      await claimButton.click();
    });

    await test.step('Bước 4: Xác minh chuyển đổi trạng thái thành công và mở quyền phê duyệt', async () => {
      const claimButton = page.getByRole('button', { name: /Nhận việc \(Claim\)/i });
      await expect(claimButton).not.toBeVisible();
      const approveBtn = page.getByRole('button', { name: /Chấp thuận/i });
      await expect(approveBtn).toBeVisible();
    });

    await test.step('Bước 5: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '05_claim_task_success', 'PASS');
    });
  });

  test('[SMOKE-06] Kiểm soát quyết định: Bắt buộc nhập lý do khi Từ chối (FR16, AT07)', async ({ page }) => {
    await test.step('Bước 1: Mở chi tiết nhiệm vụ (ID: 1)', async () => {
      await page.goto('/inbox/1');
    });

    await test.step('Bước 2: Click nút "Từ chối" để mở Modal quyết định', async () => {
      const rejectBtn = page.getByRole('button', { name: /Từ chối/i });
      await rejectBtn.click();
      const modalTitle = page.locator('text=Xác nhận từ chối hồ sơ');
      await expect(modalTitle).toBeVisible();
    });

    await test.step('Bước 3: Kiểm tra nút "Xác nhận" bị khóa khi chưa nhập lý do', async () => {
      const confirmBtn = page.getByRole('button', { name: /Xác nhận/i });
      await expect(confirmBtn).toBeDisabled();
    });

    await test.step('Bước 4: Nhập lý do từ chối và kiểm tra nút "Xác nhận" được mở', async () => {
      const reasonInput = page.getByPlaceholder(/Nhập lý do/i);
      await reasonInput.fill('Vượt ngân sách quý này');
      const confirmBtn = page.getByRole('button', { name: /Xác nhận/i });
      await expect(confirmBtn).toBeEnabled();
    });

    await test.step('Bước 5: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => {
      await captureEvidence(page, 'SCRUM-84', '06_reject_reason_validation', 'PASS');
    });
  });

});
