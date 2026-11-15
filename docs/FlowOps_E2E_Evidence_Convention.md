# Quy ước Bằng chứng Nghiệm thu Kiểm thử E2E (E2E Test Evidence Convention)

## 1. Mục đích và Quy chuẩn Evidence

Quy ước này định nghĩa cấu trúc lưu trữ và cách thức thu thập các bằng chứng kiểm thử (Evidence Artifacts) nhằm phục vụ việc nghiệm thu Pull Request (PR) và cập nhật tiến độ Jira cho Sprint 1 (Ticket `SCRUM-15` & `SCRUM-84`).

### 1.1. Thư mục lưu trữ bằng chứng
* **Thư mục ảnh chụp màn hình nghiệm thu (Evidence Screenshots):** `client/e2e-evidence/`.
* **Thư mục Báo cáo kiểm thử HTML (HTML Report):** `client/playwright-report/`.
* **Thư mục Trace / Video / Kết quả chi tiết:** `client/test-results/`.

---

### 1.2. Quy tắc đặt tên tệp ảnh Evidence
Mọi ảnh chụp màn hình bằng chứng nghiệm thu tự động phải tuân thủ đúng cú pháp chuẩn:
```text
[MÃ_TICKET]_[SỐ_THỨ_TỰ]_[TÊN_HÀNH_ĐỘNG]_[TRẠNG_THÁI].png
```

* **Danh sách 6 bằng chứng kiểm thử chuẩn của Sprint 1 (`SCRUM-84`):**
  1. `SCRUM-84_01_inbox_tabs_view_PASS.png`: Xác minh Hộp thư hiển thị đủ 2 tab Cá nhân và Nhóm.
  2. `SCRUM-84_02_inbox_group_tab_PASS.png`: Xác minh chuyển tab Nhiệm vụ nhóm và trạng thái Chờ nhận việc (`GROUP_OPEN`).
  3. `SCRUM-84_03_task_detail_snapshot_PASS.png`: Xác minh chi tiết Snapshot hồ sơ và bộ 3 nút hành động.
  4. `SCRUM-84_04_needs_assignment_alert_PASS.png`: Xác minh cảnh báo missing assignment (`NEEDS_ASSIGNMENT`).
  5. `SCRUM-84_05_claim_task_success_PASS.png`: Xác minh nút Claim biến mất và mở quyền Chấp thuận sau khi nhận việc.
  6. `SCRUM-84_06_reject_reason_validation_PASS.png`: Xác minh Modal bắt buộc nhập lý do khi Từ chối hồ sơ.

---

### 1.3. Quy chuẩn đặt tên Test Step bằng tiếng Việt (Test Steps Standard)
Nhằm giúp Báo cáo HTML trực quan, dễ hiểu đối với toàn bộ thành viên nhóm, Giảng viên và Product Owner, tất cả các ca kiểm thử E2E phải được bọc trong hàm `test.step()` có đánh số thứ tự rõ ràng bằng tiếng Việt:

```typescript
await test.step('Chuẩn bị: Đăng nhập hệ thống với quyền Người duyệt (Approver)', async () => { ... });
await test.step('Bước 1: Điều hướng vào trang Hộp thư duyệt (/inbox)', async () => { ... });
await test.step('Bước 2: Kiểm tra tiêu đề trang "Hộp thư duyệt"', async () => { ... });
await test.step('Bước 3: Chụp ảnh màn hình lưu bằng chứng nghiệm thu (Evidence)', async () => { ... });
```

---

## 2. Quy tắc xử lý các ca chưa có API Backend (Pending Policy)

Tuân thủ nghiêm ngặt nguyên tắc nghiệm thu của vé **SCRUM-15**:
> *"Chạy smoke trên môi trường test; case chưa có API đánh dấu pending, không ghi pass."*

* **Quy định triển khai:**
  1. Tuyệt đối **không được tạo mock giả vờ để ép test pass** đối với các tính năng backend chưa được TV1 / TV3 bàn giao.
  2. Các ca kiểm thử liên quan đến chức năng chưa có API được tổ chức riêng tại thư mục `client/e2e/pending/` và sử dụng cờ **`test.fixme()`**.
  3. Tất cả 4 ca pending (`[PENDING-01]` đến `[PENDING-04]`) đã được thiết kế sẵn khung kịch bản từng bước (Step tiếng Việt) theo hướng tiếp cận *Spec-First Testing*, sẵn sàng kích hoạt ngay khi Backend bàn giao ở Sprint 2:
     - `[PENDING-01]`: Xuất bản quy trình v2 và kiểm tra tính bất biến (FR04, FR07, AT08).
     - `[PENDING-02]`: Admin điều phối reassign task sự cố qua API `/coordination` (FR15, AT11).
     - `[PENDING-03]`: Kiểm tra lưu vết Audit Log bất biến vào CSDL (FR20, AT13).
     - `[PENDING-04]`: Background Scheduler quét kiểm tra SLA quá hạn (FR18, AT12).

* **Chỉ số kết quả chuẩn mực của Sprint 1:**
  - **Smoke Tests:** `6 passed`
  - **Backend Pending Tests:** `4 skipped / fixme (pending)`
  - **Failed:** `0 failed`

---

## 3. Hướng dẫn các lệnh chạy kiểm thử

Chạy các lệnh sau từ thư mục `client/`:

```powershell
# 1. Chạy bộ Smoke Test nhanh
npm run test:smoke

# 2. Chạy toàn bộ Test Suites (Bao gồm cả các ca Smoke và 4 ca Pending)
npm run test:e2e

# 3. Mở giao diện xem Báo cáo HTML trực quan trên trình duyệt
npm run test:report
