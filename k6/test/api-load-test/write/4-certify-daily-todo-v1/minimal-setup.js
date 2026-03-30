import { insertData } from "../../../../common/db/util/data-insert-runner.js";

/**
 * certifyDailyTodo trace용 최소 데이터
 *
 * - member 2명 (writer: id=1, reviewer: id=2)
 * - notification_token 1개 (reviewer)
 * - daily_todo_stats 2개
 * - challenge_group 1개 (RUNNING)
 * - challenge_group_member 2개
 * - last_selected_challenge_group_record 1개 (writer)
 * - daily_todo 1개 (CERTIFY_PENDING, writer=1, group=1)
 * - daily_todo_history 1개
 */
function createMinimalData() {
    const now = new Date();
    const startAt = new Date(now);
    startAt.setDate(startAt.getDate() - 1);
    startAt.setHours(7, 0, 0, 0);

    const endAt = new Date(startAt);
    endAt.setDate(endAt.getDate() + 29);

    const writtenAt = new Date(now);
    writtenAt.setHours(8, 0, 0, 0);

    return {
        batch_size: 100,

        // [id, provider_id, name, profile_image_url, created_at, row_inserted_at, row_updated_at]
        member_data: [
            [1, "pid-1", "writer",   "http://profile.site/1", now, now, null],
            [2, "pid-2", "reviewer", "http://profile.site/2", now, now, null],
        ],

        // [id, member_id, token_value, row_inserted_at, row_updated_at]
        notification_token_data: [
            [1, 2, "dummy-fcm-token-reviewer", now, null],
        ],

        // [id, member_id, certificated_count, approved_count, rejected_count, row_inserted_at, row_updated_at]
        daily_todo_stats_data: [
            [1, 1, 0, 0, 0, now, null],
            [2, 2, 0, 0, 0, now, null],
        ],

        // [id, name, maximum_member_count, join_code, status, start_at, end_at, created_at, row_inserted_at, row_updated_at]
        challenge_group_data: [
            [1, "trace-group", 20, "jc-trace-1", "RUNNING", startAt, endAt, startAt, now, null],
        ],

        // [id, challenge_group_id, member_id, created_at, row_inserted_at, row_updated_at]
        challenge_group_member_data: [
            [1, 1, 1, now, now, null],
            [2, 1, 2, now, now, null],
        ],

        // [id, challenge_group_id, member_id, row_inserted_at, row_updated_at]
        last_selected_challenge_group_record_data: [
            [1, 1, 1, now, null],
        ],

        // [id, challenge_group_id, writer_id, content, status, written_at, row_inserted_at, row_updated_at]
        daily_todo_data: [
            [1, 1, 1, "trace용 투두", "CERTIFY_PENDING", writtenAt, now, null],
        ],

        // [id, daily_todo_id, event_time, row_inserted_at, row_updated_at]
        daily_todo_history_data: [
            [1, 1, writtenAt, now, null],
        ],

        daily_todo_certification_data: [],
        daily_todo_certification_reviewer_data: [],
    };
}

async function setUp() {
    await insertData(createMinimalData);
}

setUp();
