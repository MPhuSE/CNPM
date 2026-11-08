# FlowOps — Gói tài liệu để khởi tạo source

Ngày: 07/11/2026. Đây là gói thiết kế, chưa phải source ứng dụng chạy được.

Đọc theo thứ tự:
1. `FlowOps_Team_Development_Design.md`: tổng quan, kiến trúc, module, API/DTO, trạng thái, workflow, cấu trúc source và lộ trình tích hợp.
2. `FlowOps_CSDL_SRS20.md`: data dictionary, transaction, constraints và các quyết định đề xuất.
3. `FlowOps_DB_SRS20.dbml`: nguồn ERD để paste vào dbdiagram.io; export SQL chưa thay cho migration đầy đủ.

Nguồn nghiệp vụ: bản SRS 2.0 người dùng cung cấp trong cuộc trao đổi. SRS và tài liệu công ty không được đóng thêm vào gói này. Các DEC/TBD vẫn cần xác nhận; tài liệu team bổ sung lựa chọn kỹ thuật, không tự thay đổi yêu cầu đã duyệt.

Cách đưa vào repository: tạo thư mục docs và đặt ba file thiết kế vào đó, tiếp theo chốt OpenAPI, tạo backend/frontend/infra theo cấu trúc trong tài liệu. Không commit tài khoản, mật khẩu hoặc tài liệu công ty vào repo công khai.
