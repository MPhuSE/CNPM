import { test, expect } from '@playwright/test';

/**
 * Các ca kiểm thử Backend / Workflow chưa hoàn thành API
 * Tuân thủ nghiêm ngặt tiêu chí nghiệm thu SCRUM-15:
 * "Case chưa có API đánh dấu pending, không ghi pass."
 * 
 * Bộ khung kịch bản được định nghĩa sẵn theo từng bước (Step) tiếng Việt
 * sẵn sàng cho việc kích hoạt và kiểm thử tự động ngay khi TV1/TV3 bàn giao API.
 */
test.describe('@pending @backend Ca kiểm thử chờ hoàn thiện API tích hợp', () => {

  test('[PENDING-01] Xuất bản quy trình v2 và kiểm tra tính bất biến (FR04, FR07, AT08)', async ({ request }) => {
    test.fixme(true, 'PENDING: Chờ TV3 hoàn thiện giao diện Workflow Builder và API xuất bản phiên bản V2.');

    await test.step('Bước 1: Gọi API tạo bản nháp phiên bản mới (v2) cho quy trình Mua sắm (PROCUREMENT)', async () => {
      // const res = await request.post('/api/v1/workflows/1/versions', {
      //   data: { version: 2, description: 'Bổ sung bước IT duyệt an toàn thông tin' }
      // });
      // expect(res.status()).toBe(201);
    });

    await test.step('Bước 2: Xuất bản (Publish) phiên bản v2 sang trạng thái ACTIVE', async () => {
      // const publishRes = await request.post('/api/v1/workflow-versions/2/publish');
      // expect(publishRes.status()).toBe(200);
    });

    await test.step('Bước 3: Xác minh tính bất biến - Hồ sơ cũ đã tạo ở v1 vẫn tiếp tục chạy theo luồng v1', async () => {
      // const runningReq = await request.get('/api/v1/requests/REQ-2026-001');
      // const data = await runningReq.json();
      // expect(data.workflowVersion).toBe(1);
    });

    await test.step('Bước 4: Xác minh hồ sơ mới tạo sau thời điểm publish tự động áp dụng phiên bản v2', async () => {
      // const newReq = await request.post('/api/v1/requests', { data: { workflowId: 1, title: 'Đơn mới theo v2' } });
      // const data = await newReq.json();
      // expect(data.workflowVersion).toBe(2);
    });
  });

  test('[PENDING-02] Admin điều phối reassign task sự cố qua API /coordination (FR15, AT11)', async ({ request }) => {
    test.fixme(true, 'PENDING: Chờ TV1 hoàn thiện Backend endpoint /tasks/{id}/reassign và logic ghi nhật ký điều phối.');

    await test.step('Bước 1: Truy vấn danh sách nhiệm vụ gặp sự cố (NEEDS_ASSIGNMENT) trong hàng đợi điều phối', async () => {
      // const res = await request.get('/api/v1/coordination/tasks?status=NEEDS_ASSIGNMENT');
      // expect(res.status()).toBe(200);
    });

    await test.step('Bước 2: Admin gửi lệnh Reassign chỉ định người duyệt mới (Assignee ID: 3)', async () => {
      // const reassignRes = await request.post('/api/v1/tasks/2/reassign', {
      //   data: { newAssigneeId: '3', reason: 'Quản lý trực tiếp nghỉ phép đột xuất' }
      // });
      // expect(reassignRes.status()).toBe(200);
    });

    await test.step('Bước 3: Xác minh trạng thái nhiệm vụ chuyển sang ASSIGNED và giữ nguyên hạn SLA (due_at)', async () => {
      // const updatedTask = await request.get('/api/v1/tasks/2');
      // const data = await updatedTask.json();
      // expect(data.assignmentState).toBe('ASSIGNED');
      // expect(data.assigneeId).toBe('3');
    });

    await test.step('Bước 4: Xác minh sự kiện điều phối được ghi nhận vào nhật ký sự cố', async () => {
      // const events = await request.get('/api/v1/coordination/events?taskId=2');
      // expect(events.status()).toBe(200);
    });
  });

  test('[PENDING-03] Kiểm tra lưu vết Audit Log bất biến vào CSDL (FR20, AT13)', async ({ request }) => {
    test.fixme(true, 'PENDING: Chờ TV1 tích hợp RequestEventJpaEntity và API tra cứu nhật ký /audit.');

    await test.step('Bước 1: Thực hiện thao tác phê duyệt nhiệm vụ hợp lệ để kích hoạt ghi vết Audit', async () => {
      // const approveRes = await request.post('/api/v1/tasks/1/approve', {
      //   data: { comment: 'Đồng ý phê duyệt đề xuất' }
      // });
      // expect(approveRes.status()).toBe(200);
    });

    await test.step('Bước 2: Gọi API tra cứu lịch sử sự kiện (/api/v1/audit-logs) của hồ sơ', async () => {
      // const auditRes = await request.get('/api/v1/audit-logs?requestId=REQ-2026-001');
      // expect(auditRes.status()).toBe(200);
    });

    await test.step('Bước 3: Xác minh bản ghi Audit chứa đủ thông tin bắt buộc (Actor, Timestamp, IP, Payload)', async () => {
      // const logs = await auditRes.json();
      // const lastEvent = logs[0];
      // expect(lastEvent.eventType).toBe('TASK_APPROVED');
      // expect(lastEvent.actorId).toBeDefined();
      // expect(lastEvent.createdAt).toBeDefined();
    });

    await test.step('Bước 4: Kiểm tra tính bất biến (Append-Only) - Không có API cập nhật hay xóa bản ghi Audit', async () => {
      // const deleteRes = await request.delete('/api/v1/audit-logs/1');
      // expect(deleteRes.status()).toBe(405); // Method Not Allowed
    });
  });

  test('[PENDING-04] Background Scheduler quét kiểm tra SLA quá hạn (FR18, AT12)', async ({ request }) => {
    test.fixme(true, 'PENDING: Chờ cấu hình Spring Scheduler chạy nền trên môi trường staging/test.');

    await test.step('Bước 1: Chuẩn bị dữ liệu kiểm thử - Nhiệm vụ có hạn xử lý (due_at) trong quá khứ', async () => {
      // Nhiệm vụ mẫu có due_at = now() - 1 hour, isOverdue = false
    });

    await test.step('Bước 2: Kích hoạt Scheduler quét định kỳ (hoặc chờ cron job thực thi)', async () => {
      // Scheduler chạy theo cấu hình cron: 0 * * * * * (mỗi phút 1 lần)
      // await request.post('/api/v1/admin/scheduler/trigger-sla-scan');
    });

    await test.step('Bước 3: Xác minh cờ isOverdue được cập nhật thành true trong CSDL', async () => {
      // const taskRes = await request.get('/api/v1/tasks/99');
      // const taskData = await taskRes.json();
      // expect(taskData.isOverdue).toBe(true);
    });

    await test.step('Bước 4: Xác minh thông báo in-app cảnh báo quá hạn được gửi tới Người duyệt', async () => {
      // const notiRes = await request.get('/api/v1/notifications?userId=approver');
      // const notiData = await notiRes.json();
      // expect(notiData.some((n: any) => n.type === 'SLA_BREACH_WARNING')).toBe(true);
    });
  });

});
