# FlowOps - Ma trận Truy vết FR–AT và Dữ liệu Kiểm thử (Test Traceability Matrix & Test Data)

## 1. Tổng quan phạm vi và mục tiêu kiểm thử

Ma trận này bao phủ toàn bộ:
* **21 Yêu cầu chức năng (FR01 - FR21)** và **14 Tiêu chuẩn nghiệm thu (AT01 - AT14)**.
* **3 Vai trò người dùng:** `ROLE_EMPLOYEE` (Nhân viên), `ROLE_APPROVER` (Người duyệt), `ROLE_ADMIN` (Quản trị viên).
* **3 Quy trình mẫu:** 
  1. `PROCUREMENT` - Mua sắm thiết bị
  2. `EXPENSE_REIMBURSEMENT` - Hoàn ứng chi phí
  3. `ACCESS_REQUEST` - Cấp quyền truy cập hệ thống

---

> [NOTE]
> * **Căn cứ truy vết Yêu cầu chức năng (FR13 – FR17):** Thuộc phân hệ *Hộp thư duyệt, Ra quyết định & Toàn vẹn dữ liệu* do TV4 chủ trì, được quy định tại **Mục c (Các chức năng và màn hình, dòng 134–138)** và đặc tả luồng chi tiết tại **Mục WF04 (Duyệt task cá nhân/nhóm), Mục WF07 (Phân công lại), Mục e & h (Xử lý xung đột 409 & Idempotency Key)** trong tài liệu SAD `docs/FlowOps_Team_Development_Design.md`.
> * **Căn cứ truy vết Tiêu chuẩn chấp nhận (AT01 – AT14):** Danh mục 14 tiêu chuẩn chấp nhận kỹ thuật AT01 đến AT14 được định nghĩa chuẩn hóa tại **Mục k (Kiểm thử triển khai và vận hành — Ma trận nghiệm thu kỹ thuật, dòng 748–758)** trong tài liệu SAD `docs/FlowOps_Team_Development_Design.md`.
>
> Ma trận dưới đây trực tiếp kế thừa và ánh xạ chi tiết các yêu cầu FR và mã AT trên vào từng ca kiểm thử thực thi cụ thể.

---

## 2. Đặc tả Bộ dữ liệu kiểm thử chuẩn (Test Data Specification)

### 2.1. Danh sách tài khoản kiểm thử (Actors)
| Username | Password | Vai trò | Chức danh / Phòng ban | Mục đích kiểm thử |
|---|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_EMPLOYEE` | Quản trị viên hệ thống (BOD) | Điều phối, xem audit, cấu hình workflow, giải quyết sự cố |
| `employee` | `employee123` | `ROLE_EMPLOYEE` | Nhân viên kinh doanh (SALES) | Tạo nháp, nộp hồ sơ, sửa bổ sung, hủy đơn |
| `manager_alice` | `approver123` | `ROLE_EMPLOYEE`, `ROLE_APPROVER` | Quản lý trực tiếp của `employee` (SALES) | Duyệt bước 1 (Quản lý trực tiếp), test Anti-Self-Approval |
| `finance_bob` | `approver123` | `ROLE_APPROVER` | Thành viên Ban Tài chính (FIN) | Claim & duyệt bước 2 (Tài chính >= 10M) |
| `it_charlie` | `approver123` | `ROLE_APPROVER` | IT Support Lead (IT) | Claim & duyệt cấp quyền truy cập |

---

### 2.2. Bộ dữ liệu kiểm thử giá trị biên tiền tệ (Boundary Values)
Áp dụng cho quy trình **Mua sắm thiết bị (PROCUREMENT)**: Quy tắc rẽ nhánh: `Nếu amount >= 10.000.000 VND -> Bước 2 (Ban Tài chính), ngược lại -> END_APPROVED`.

| Mã ca kiểm thử | Giá trị đầu vào (`amount`) | Nhánh rẽ kỳ vọng | Người duyệt kế tiếp | Trạng thái sau bước 1 |
|---|---|---|---|---|
| **BV-01 (Cận dưới)** | `9.999.999` (VND) | Không thỏa mãn GTE 10M | Không có bước 2 | Kết thúc luồng $\rightarrow$ `APPROVED` |
| **BV-02 (Tại biên)** | `10.000.000` (VND) | Thỏa mãn GTE 10M | Ban Tài chính (`FINANCE`) | Chuyển sang bước 2 $\rightarrow$ `PENDING` |
| **BV-03 (Cận trên)** | `10.000.001` (VND) | Thỏa mãn GTE 10M | Ban Tài chính (`FINANCE`) | Chuyển sang bước 2 $\rightarrow$ `PENDING` |

---

### 2.3. Dữ liệu kiểm thử ngoại lệ và vòng đời (Exceptions & Revisions)
1. **Ca thiếu Quản lý trực tiếp (Missing Manager):**
   * *Đầu vào:* Tài khoản tạo đơn có `manager_id = NULL`.
   * *Kỳ vọng:* Task sinh ra ở trạng thái `assignment_state = NEEDS_ASSIGNMENT`, `assignee_id = NULL`. SLA vẫn bắt đầu tính. Đơn hiển thị trong hàng đợi `/coordination` của Admin.
2. **Ca nhóm duyệt rỗng (Missing / Empty Group):**
   * *Đầu vào:* Bước yêu cầu gán cho Group nhưng group không có thành viên nào mang cờ `is_active = true`.
   * *Kỳ vọng:* Task sinh ra `assignment_state = NEEDS_ASSIGNMENT`. Admin phải Reassign thủ công.
3. **Ca sửa đổi bổ sung - Quay lại từ đầu (Revision Restart):**
   * *Đầu vào:* Yêu cầu bổ sung $\rightarrow$ Sửa trường `amount` từ 9.000.000 thành 12.000.000 (trường có `affectsApproval = true`).
   * *Kỳ vọng:* `restart_required = true`, đơn quay lại Bước 1 (Quản lý duyệt lại từ đầu).
4. **Ca sửa đổi bổ sung - Tiếp tục (Revision Continue):**
   * *Đầu vào:* Yêu cầu bổ sung $\rightarrow$ Chỉ thay đổi `receiptUrl` (trường có `affectsApproval = false`).
   * *Kỳ vọng:* `restart_required = false`, đơn tiếp tục tại bước yêu cầu bổ sung mà không bắt duyệt lại từ đầu.
5. **Ca xuất bản phiên bản mới (Publish v2 & Immutability):**
   * *Đầu vào:* Xuất bản Version 2 với cấu hình form/bước mới trong khi có 1 đơn đang chạy ở Version 1.
   * *Kỳ vọng:* Đơn cũ tiếp tục chạy theo đúng cấu trúc Version 1; đơn tạo mới sau thời điểm publish nhận Version 2.

---

## 3. Ma trận Truy vết chi tiết (FR–AT Traceability Matrix)

| Mã TC | Nguồn tài liệu gốc | Yêu cầu FR & AT | Vai trò | Tiền điều kiện (Precondition) | Dữ liệu đầu vào (Input) | Kết quả mong đợi (Expected Outcome) | Owner (Feature / QA) |
|---|---|---|---|---|---|---|---|
| **TC01** | SRS 2.0 §4.1, SAD §b.1 | FR01, FR03 (AT01) | All | User chưa đăng nhập | Username & password hợp lệ (`admin` / `admin123`) | Trả về JWT Access Token (15p), tạo UserSession trong CSDL, FE nhận thông tin user & roles | TV1 (BE) / TV2 (FE) |
| **TC02** | SRS 2.0 §4.1, SAD §b.1 | FR01, FR03 (AT01) | All | User đã đăng nhập | Gọi `/api/v1/auth/logout` | Session bị thu hồi, Access token bị hủy trên client, điều hướng về `/login` | TV1 (BE) / TV2 (FE) |
| **TC03** | SRS 2.0 §4.2, SAD §a (SoD) | FR02, FR13 (AT02) | Approver | `manager_alice` nộp hồ sơ mua sắm | `manager_alice` vào `/inbox` tìm task | **Anti-Self-Approval:** Alice tuyệt đối không thấy nút Claim hoặc Approve trên hồ sơ của mình | TV4 (Inbox & QA) |
| **TC04** | SRS 2.0 §4.4, DB §V6 | FR04, FR06 (AT08) | Admin | Có định nghĩa quy trình `DRAFT` | Bấm "Xuất bản" (Publish) | Quy trình chuyển `PUBLISHED`, sinh version bất biến (trigger V6 chặn sửa đổi published) | TV3 (Workflow Builder) |
| **TC05** | SRS 2.0 §4.5, SAD §WF01 | FR08, FR09 (AT03) | Employee | Workflow `PROCUREMENT` v1 active | Tạo đơn với số tiền `9.999.999 VND` | Đơn chuyển `PENDING` (Rev #1), tạo Task cho Quản lý trực tiếp (`manager_alice`) | TV2 (My-Requests) |
| **TC06** | SRS 2.0 §4.6, SAD §WF04 | FR13, FR14 (AT03) | Approver | Task bước 1 gán cho Alice, amount = 9.999.999 | Alice bấm "Chấp thuận" (Approve) | Đơn không rẽ nhánh Tài chính $\rightarrow$ Chuyển `APPROVED`, Request hoàn tất | TV4 (Inbox & QA) |
| **TC07** | SRS 2.0 §4.6, SAD §WF04 | FR13, FR14 (AT03) | Approver | Task bước 1 gán cho Alice, amount = 10.000.000 | Alice bấm "Chấp thuận" (Approve) | Đơn rẽ nhánh GTE 10M $\rightarrow$ Sinh Task cho nhóm `FINANCE` (`assignment_state=GROUP_OPEN`) | TV4 (Inbox & QA) |
| **TC08** | SRS 2.0 §4.6, SAD §WF04 | FR13, FR15 (AT06) | Approver | Task nhóm Finance đang ở `GROUP_OPEN` | `finance_bob` bấm "Nhận việc (Claim)" | Task chuyển sang `assignee_id = Bob`, `assignment_state = ASSIGNED` | TV4 (Inbox & QA) |
| **TC09** | SRS 2.0 §4.7, SAD §WF04 | FR15 (AT06) | Approver | Hai approver cùng bấm Claim 1 task cùng lúc | Gửi 2 request Claim với `taskVersion = 0` | Người đến trước Claim thành công; người đến sau nhận lỗi `409 Concurrency Conflict` | TV4 (QA E2E) |
| **TC10** | SRS 2.0 §4.6, SAD §WF04 | FR14, FR16 (AT04) | Approver | Task đã được claim | Bob bấm "Yêu cầu bổ sung", nhập lý do "Thiếu hóa đơn VAT" | Task hoàn tất, Request chuyển `RETURNED`, mở bản sao `request_edits` cho Employee | TV4 (Inbox & QA) |
| **TC11** | SRS 2.0 §4.5, SAD §WF05 | FR11, FR12 (AT05) | Employee | Request ở trạng thái `RETURNED` | Employee chỉ sửa `receiptUrl` (không đổi tiền) $\rightarrow$ Bấm Nộp lại | Sinh Revision #2, `restart_required = false`, hồ sơ quay lại bước Tài chính | TV2 (FE) / TV4 (QA) |
| **TC12** | SRS 2.0 §4.5, SAD §WF05 | FR11, FR12 (AT05) | Employee | Request ở trạng thái `RETURNED` | Employee đổi số tiền từ 10M lên 15M $\rightarrow$ Bấm Nộp lại | Sinh Revision #2, `restart_required = true`, hồ sơ quay lại bước 1 (Alice duyệt lại) | TV2 (FE) / TV4 (QA) |
| **TC13** | SRS 2.0 §4.5, SAD §WF03 | FR10 (AT09) | Employee | Request đang `PENDING` | Employee bấm "Hủy hồ sơ" kèm lý do | Request chuyển `CANCELLED`, các task PENDING bị hủy, không thể duyệt tiếp | TV2 (My-Requests) |
| **TC14** | SRS 2.0 §4.6, SAD §WF04 | FR14 (AT07) | Approver | Task đã gán cho Bob | Bob bấm "Từ chối" (Reject) kèm lý do | Request chuyển `REJECTED`, toàn bộ luồng dừng lại, gửi thông báo in-app cho người gửi | TV4 (Inbox & QA) |
| **TC15** | SRS 2.0 §4.6, SAD §WF07 | FR13, FR15 (AT10) | Admin | Employee không có quản lý trực tiếp | Nộp đơn mua sắm thiết bị | Sinh Task `NEEDS_ASSIGNMENT`, hiển thị tại `/coordination` để Admin gán người hợp lệ | TV4 (Coordination) |
| **TC16** | SRS 2.0 §4.6, SAD §WF07 | FR15 (AT11) | Admin | Task `NEEDS_ASSIGNMENT` trong `/coordination` | Admin chọn người duyệt mới hợp lệ và bấm Phân công lại | Task chuyển sang người mới, due_at giữ nguyên từ mốc khởi tạo, ghi log audit | TV4 (Coordination) |
| **TC17** | SRS 2.0 §4.7, SAD §e | FR17 (AT04) | All | Mạng chập chờn / timeout | Client gửi lại command cùng `Idempotency-Key` | Backend trả về kết quả cũ đã commit, không sinh duplicate task/log/notification | TV1 (BE) / TV4 (QA) |
| **TC18** | SRS 2.0 §4.8, SAD §WF08 | FR18, FR19 (AT12) | System | Task vượt mốc `due_at` (SLA quá hạn) | Scheduler quét hằng phút (`cron: 0 * * * * *`) | Task được đánh dấu overdue, tạo thông báo in-app nhắc nhở người duyệt | TV1 (BE) / TV4 (QA) |
| **TC19** | SRS 2.0 §4.9, DB §V4 | FR20 (AT13) | Admin | Mọi hành động duyệt/hủy/reassign diễn ra | Admin vào `/audit` tra cứu | Bảng audit logs ghi nhận bất biến (IP, timestamp, actor, command, payload canonical) | TV3 (FE) / TV4 (QA) |
| **TC20** | SRS 2.0 §4.9, SAD §c | FR21 (AT13) | Admin | Đã có các hồ sơ ở nhiều trạng thái | Admin vào `/dashboard` xem báo cáo | Hiển thị chính xác metrics: Tổng đã nộp, Đang duyệt, Đã duyệt, Từ chối, Quá hạn, Cần gán | TV4 (Dashboard & QA) |
| **TC21** | SRS 2.0 §4.4, SAD §WF02 | FR06, FR07 (AT08) | Admin | Xuất bản Workflow v2 (thêm field mới) | Nộp đơn mới theo v2 và kiểm tra đơn cũ v1 | Đơn cũ vẫn hoàn thành theo schema v1; đơn mới áp dụng đúng quy tắc v2 | TV3 (Builder) / TV4 (QA) |

