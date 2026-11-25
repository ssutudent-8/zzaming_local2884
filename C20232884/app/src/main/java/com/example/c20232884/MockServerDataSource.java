package com.example.c20232884;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.c20232884.model.Comment;
import com.example.c20232884.model.Post;
import com.example.c20232884.model.ScheduleItem;
import com.example.c20232884.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 파이어스토어 비동기 동기화 및 SharedPreferences 데이터 영구 보존을 담당하는 더미 서버 Singleton 클래스
 */
public class MockServerDataSource {

    private static final String PREF_NAME = "c20232884_db_prefs";
    private static final String KEY_SCHEDULES = "key_schedules";
    private static final String KEY_POSTS = "key_posts";
    private static final String KEY_COMMENTS = "key_comments";
    private static final String KEY_SOLDIER_USER = "key_soldier_user";
    private static final String KEY_OFFICER_USER = "key_officer_user";
    private static final String KEY_MEMBER_TYPE = "key_member_type";

    private static MockServerDataSource instance;

    private final List<ScheduleItem> serverUnitSchedules;
    private final List<Post> serverPosts;
    private final List<Comment> serverComments;

    private User dummySoldier;
    private User dummyOfficer;
    private User currentUser;

    private SharedPreferences sharedPreferences;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 비동기 콜백 리스너 인터페이스 정의
    public interface OnScheduleLoadListener {
        void onSuccess(List<ScheduleItem> scheduleList);
        void onFailure(Exception e);
    }

    public interface OnPostLoadListener {
        void onSuccess(List<Post> postList);
        void onFailure(Exception e);
    }

    public interface OnCommentLoadListener {
        void onSuccess(List<Comment> commentList);
        void onFailure(Exception e);
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    private MockServerDataSource() {
        serverUnitSchedules = new ArrayList<>();
        serverPosts = new ArrayList<>();
        serverComments = new ArrayList<>();

        dummySoldier = new User("u_soldier_01", "김병장", "병사", "백골대대", "병장", "2025-06-16", "2026-12-15");
        dummyOfficer = new User("u_officer_01", "행정관", "간부", "백골대대", "상사", "2020-03-01", "2030-12-31");
        currentUser = dummySoldier;
    }

    public static synchronized MockServerDataSource getInstance() {
        if (instance == null) {
            instance = new MockServerDataSource();
        }
        return instance;
    }

    public static synchronized MockServerDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new MockServerDataSource();
        }
        if (context != null && instance.sharedPreferences == null) {
            instance.initSharedPreferences(context.getApplicationContext());
        }
        return instance;
    }

    public void initSharedPreferences(Context context) {
        if (context == null) return;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadAllDataFromPrefs();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void selectMemberType(String memberType) {
        if ("간부".equals(memberType)) {
            this.currentUser = dummyOfficer;
        } else {
            this.currentUser = dummySoldier;
        }
        saveUsersToPrefs();
    }

    public User getDummySoldier() {
        return dummySoldier;
    }

    public User getDummyOfficer() {
        return dummyOfficer;
    }

    // =============================================================================================
    // 비동기 파이어스토어 호환 콜백 리스너 메서드 (Async Listener APIs)
    // =============================================================================================

    public void fetchServerUnitSchedules(OnScheduleLoadListener listener) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess(new ArrayList<>(serverUnitSchedules));
            }
        });
    }

    public void publishUnitSchedule(ScheduleItem schedule, OnCompleteListener listener) {
        if (schedule != null && "부대".equals(schedule.getType())) {
            serverUnitSchedules.add(0, schedule);
            saveSchedulesToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void fetchServerPosts(OnPostLoadListener listener) {
        mainHandler.post(() -> {
            attachCommentsToPosts();
            if (listener != null) {
                listener.onSuccess(new ArrayList<>(serverPosts));
            }
        });
    }

    public void publishPost(Post post, OnCompleteListener listener) {
        if (post != null) {
            serverPosts.add(0, post);
            if (post.getComments() != null && !post.getComments().isEmpty()) {
                for (Comment comment : post.getComments()) {
                    comment.setPostId(post.getId());
                    serverComments.add(comment);
                }
            }
            savePostsToPrefs();
            saveCommentsToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void deletePost(String postId, OnCompleteListener listener) {
        if (postId != null) {
            Iterator<Post> iterator = serverPosts.iterator();
            while (iterator.hasNext()) {
                Post post = iterator.next();
                if (postId.equals(post.getId())) {
                    iterator.remove();
                    break;
                }
            }

            Iterator<Comment> commentIterator = serverComments.iterator();
            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                if (postId.equals(comment.getPostId())) {
                    commentIterator.remove();
                }
            }
            savePostsToPrefs();
            saveCommentsToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void updatePostLikes(String postId, int newLikes, OnCompleteListener listener) {
        if (postId != null) {
            for (Post post : serverPosts) {
                if (postId.equals(post.getId())) {
                    post.setLikes(newLikes);
                    break;
                }
            }
            savePostsToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void fetchCommentsForPost(String postId, OnCommentLoadListener listener) {
        mainHandler.post(() -> {
            List<Comment> result = new ArrayList<>();
            if (postId != null) {
                for (Comment comment : serverComments) {
                    if (postId.equals(comment.getPostId())) {
                        result.add(comment);
                    }
                }
            }
            if (listener != null) {
                listener.onSuccess(result);
            }
        });
    }

    public void addCommentToPost(String postId, Comment comment, OnCompleteListener listener) {
        if (postId != null && comment != null) {
            comment.setPostId(postId);
            serverComments.add(comment);
            for (Post post : serverPosts) {
                if (postId.equals(post.getId())) {
                    post.getComments().add(comment);
                    break;
                }
            }
            saveCommentsToPrefs();
            savePostsToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void deleteCommentFromPost(String postId, String commentId, OnCompleteListener listener) {
        if (commentId != null) {
            Iterator<Comment> iterator = serverComments.iterator();
            while (iterator.hasNext()) {
                Comment comment = iterator.next();
                if (commentId.equals(comment.getId())) {
                    iterator.remove();
                    break;
                }
            }
            if (postId != null) {
                for (Post post : serverPosts) {
                    if (postId.equals(post.getId()) && post.getComments() != null) {
                        Iterator<Comment> postCommentIter = post.getComments().iterator();
                        while (postCommentIter.hasNext()) {
                            if (commentId.equals(postCommentIter.next().getId())) {
                                postCommentIter.remove();
                                break;
                            }
                        }
                    }
                }
            }
            saveCommentsToPrefs();
            savePostsToPrefs();
        }
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    public void updateUserDates(String userId, String enlistmentDate, String dischargeDate, OnCompleteListener listener) {
        if (dummySoldier != null && dummySoldier.getId().equals(userId)) {
            dummySoldier.setEnlistmentDate(enlistmentDate);
            dummySoldier.setDischargeDate(dischargeDate);
        } else if (dummyOfficer != null && dummyOfficer.getId().equals(userId)) {
            dummyOfficer.setEnlistmentDate(enlistmentDate);
            dummyOfficer.setDischargeDate(dischargeDate);
        }
        saveUsersToPrefs();
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onSuccess();
            }
        });
    }

    // =============================================================================================
    // 동기식 헬퍼 메서드 (Synchronous Helper APIs for Backwards Compatibility)
    // =============================================================================================

    public List<ScheduleItem> fetchServerUnitSchedules() {
        return new ArrayList<>(serverUnitSchedules);
    }

    public void publishUnitSchedule(ScheduleItem schedule) {
        publishUnitSchedule(schedule, null);
    }

    public List<Post> fetchServerPosts() {
        attachCommentsToPosts();
        return new ArrayList<>(serverPosts);
    }

    public void publishPost(Post post) {
        publishPost(post, null);
    }

    public void deletePost(String postId) {
        deletePost(postId, null);
    }

    public List<Comment> fetchCommentsForPost(String postId) {
        List<Comment> result = new ArrayList<>();
        if (postId != null) {
            for (Comment comment : serverComments) {
                if (postId.equals(comment.getPostId())) {
                    result.add(comment);
                }
            }
        }
        return result;
    }

    public void addCommentToPost(String postId, Comment comment) {
        addCommentToPost(postId, comment, null);
    }

    public void deleteCommentFromPost(String postId, String commentId) {
        deleteCommentFromPost(postId, commentId, null);
    }

    // =============================================================================================
    // SharedPreferences 데이터 읽기/쓰기 및 초기화 (JSON Data Persistence)
    // =============================================================================================

    private void attachCommentsToPosts() {
        for (Post post : serverPosts) {
            List<Comment> postComments = new ArrayList<>();
            for (Comment comment : serverComments) {
                if (post.getId() != null && post.getId().equals(comment.getPostId())) {
                    postComments.add(comment);
                }
            }
            post.setComments(postComments);
        }
    }

    private void loadAllDataFromPrefs() {
        if (sharedPreferences == null) {
            initDefaultData();
            return;
        }

        String schedulesJson = sharedPreferences.getString(KEY_SCHEDULES, null);
        String postsJson = sharedPreferences.getString(KEY_POSTS, null);
        String commentsJson = sharedPreferences.getString(KEY_COMMENTS, null);
        String soldierJson = sharedPreferences.getString(KEY_SOLDIER_USER, null);
        String officerJson = sharedPreferences.getString(KEY_OFFICER_USER, null);
        String memberType = sharedPreferences.getString(KEY_MEMBER_TYPE, "병사");

        if (schedulesJson == null || postsJson == null) {
            initDefaultData();
            saveAllToPrefs();
            return;
        }

        try {
            serverUnitSchedules.clear();
            JSONArray schedArray = new JSONArray(schedulesJson);
            for (int i = 0; i < schedArray.length(); i++) {
                JSONObject obj = schedArray.getJSONObject(i);
                serverUnitSchedules.add(new ScheduleItem(
                        obj.optString("id", "sched_" + i),
                        obj.optString("date", ""),
                        obj.optString("title", ""),
                        obj.optString("startTime", ""),
                        obj.optString("endTime", ""),
                        obj.optString("location", ""),
                        obj.optString("type", "부대"),
                        obj.optString("memo", "")
                ));
            }

            serverComments.clear();
            if (commentsJson != null) {
                JSONArray commentArray = new JSONArray(commentsJson);
                for (int i = 0; i < commentArray.length(); i++) {
                    JSONObject obj = commentArray.getJSONObject(i);
                    serverComments.add(new Comment(
                            obj.optString("id", "c_" + i),
                            obj.optString("postId", ""),
                            obj.optString("authorUid", ""),
                            obj.optString("authorName", ""),
                            obj.optString("content", ""),
                            obj.optString("createdAt", "")
                    ));
                }
            }

            serverPosts.clear();
            JSONArray postArray = new JSONArray(postsJson);
            for (int i = 0; i < postArray.length(); i++) {
                JSONObject obj = postArray.getJSONObject(i);
                Post post = new Post(
                        obj.optString("id", "p_" + i),
                        obj.optString("title", ""),
                        obj.optString("content", ""),
                        obj.optString("authorUid", ""),
                        obj.optString("authorName", ""),
                        obj.optString("createdAt", ""),
                        new ArrayList<>(),
                        obj.optString("category", "자유")
                );
                post.setLikes(obj.optInt("likes", 0));
                serverPosts.add(post);
            }
            attachCommentsToPosts();

            if (soldierJson != null) {
                JSONObject sObj = new JSONObject(soldierJson);
                dummySoldier = new User(
                        sObj.optString("id", "u_soldier_01"),
                        sObj.optString("userName", "김병장"),
                        sObj.optString("memberType", "병사"),
                        sObj.optString("unitId", "백골대대"),
                        sObj.optString("rank", "병장"),
                        sObj.optString("enlistmentDate", "2025-06-16"),
                        sObj.optString("dischargeDate", "2026-12-15")
                );
            }

            if (officerJson != null) {
                JSONObject oObj = new JSONObject(officerJson);
                dummyOfficer = new User(
                        oObj.optString("id", "u_officer_01"),
                        oObj.optString("userName", "행정관"),
                        oObj.optString("memberType", "간부"),
                        oObj.optString("unitId", "백골대대"),
                        oObj.optString("rank", "상사"),
                        oObj.optString("enlistmentDate", "2020-03-01"),
                        oObj.optString("dischargeDate", "2030-12-31")
                );
            }

            selectMemberType(memberType);

        } catch (JSONException e) {
            initDefaultData();
            saveAllToPrefs();
        }
    }

    private void initDefaultData() {
        serverUnitSchedules.clear();
        serverUnitSchedules.add(new ScheduleItem("sched_01", "2026-10-01", "국군의 날 행사", "10:00", "12:00", "연병장", "부대", "정복 착용"));
        serverUnitSchedules.add(new ScheduleItem("sched_02", "2026-11-15", "동계 진지공사", "08:00", "17:00", "진지 A", "부대", "방한장갑 준비"));

        serverComments.clear();
        serverComments.add(new Comment("c1", "p1", "u_soldier_02", "이일병", "체육대회 일정 확인했습니다!", "1시간 전"));
        serverComments.add(new Comment("c2", "p1", "u_soldier_03", "박상병", "준비물 챙기겠습니다.", "30분 전"));

        serverPosts.clear();
        Post post1 = new Post(
                "p1",
                "6월 대대 체육대회 및 일정 안내",
                "이번 주 금요일 대대 전투체육 일정 안내드립니다. 모두 전투복 및 운동화 착용 바랍니다.",
                "u_officer_01",
                "행정관",
                "1시간 전",
                new ArrayList<>(),
                "공지"
        );

        Post post2 = new Post(
                "p2",
                "마음의 편지함 점검 안내",
                "익명성이 보장되는 부대 개선 마음의 편지함이 열려 있으니 많은 의견 바랍니다.",
                "u_officer_01",
                "행정관",
                "3시간 전",
                new ArrayList<>(),
                "공지"
        );

        serverPosts.add(post1);
        serverPosts.add(post2);
        attachCommentsToPosts();

        dummySoldier = new User("u_soldier_01", "김병장", "병사", "백골대대", "병장", "2025-06-16", "2026-12-15");
        dummyOfficer = new User("u_officer_01", "행정관", "간부", "백골대대", "상사", "2020-03-01", "2030-12-31");
        currentUser = dummySoldier;
    }

    private void saveAllToPrefs() {
        saveSchedulesToPrefs();
        savePostsToPrefs();
        saveCommentsToPrefs();
        saveUsersToPrefs();
    }

    private void saveSchedulesToPrefs() {
        if (sharedPreferences == null) return;
        JSONArray array = new JSONArray();
        for (ScheduleItem item : serverUnitSchedules) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", item.getId());
                obj.put("date", item.getDate());
                obj.put("title", item.getTitle());
                obj.put("startTime", item.getStartTime());
                obj.put("endTime", item.getEndTime());
                obj.put("location", item.getLocation());
                obj.put("type", item.getType());
                obj.put("memo", item.getMemo());
                array.put(obj);
            } catch (JSONException ignored) {}
        }
        sharedPreferences.edit().putString(KEY_SCHEDULES, array.toString()).apply();
    }

    private void savePostsToPrefs() {
        if (sharedPreferences == null) return;
        JSONArray array = new JSONArray();
        for (Post post : serverPosts) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", post.getId());
                obj.put("title", post.getTitle());
                obj.put("content", post.getContent());
                obj.put("authorUid", post.getAuthorUid());
                obj.put("authorName", post.getAuthorName());
                obj.put("createdAt", post.getCreatedAt());
                obj.put("likes", post.getLikes());
                obj.put("category", post.getCategory());
                array.put(obj);
            } catch (JSONException ignored) {}
        }
        sharedPreferences.edit().putString(KEY_POSTS, array.toString()).apply();
    }

    private void saveCommentsToPrefs() {
        if (sharedPreferences == null) return;
        JSONArray array = new JSONArray();
        for (Comment comment : serverComments) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", comment.getId());
                obj.put("postId", comment.getPostId());
                obj.put("authorUid", comment.getAuthorUid());
                obj.put("authorName", comment.getAuthorName());
                obj.put("content", comment.getContent());
                obj.put("createdAt", comment.getCreatedAt());
                array.put(obj);
            } catch (JSONException ignored) {}
        }
        sharedPreferences.edit().putString(KEY_COMMENTS, array.toString()).apply();
    }

    private void saveUsersToPrefs() {
        if (sharedPreferences == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (dummySoldier != null) {
            JSONObject sObj = new JSONObject();
            try {
                sObj.put("id", dummySoldier.getId());
                sObj.put("userName", dummySoldier.getUserName());
                sObj.put("memberType", dummySoldier.getMemberType());
                sObj.put("unitId", dummySoldier.getUnitId());
                sObj.put("rank", dummySoldier.getRank());
                sObj.put("enlistmentDate", dummySoldier.getEnlistmentDate());
                sObj.put("dischargeDate", dummySoldier.getDischargeDate());
                editor.putString(KEY_SOLDIER_USER, sObj.toString());
            } catch (JSONException ignored) {}
        }

        if (dummyOfficer != null) {
            JSONObject oObj = new JSONObject();
            try {
                oObj.put("id", dummyOfficer.getId());
                oObj.put("userName", dummyOfficer.getUserName());
                oObj.put("memberType", dummyOfficer.getMemberType());
                oObj.put("unitId", dummyOfficer.getUnitId());
                oObj.put("rank", dummyOfficer.getRank());
                oObj.put("enlistmentDate", dummyOfficer.getEnlistmentDate());
                oObj.put("dischargeDate", dummyOfficer.getDischargeDate());
                editor.putString(KEY_OFFICER_USER, oObj.toString());
            } catch (JSONException ignored) {}
        }

        if (currentUser != null) {
            editor.putString(KEY_MEMBER_TYPE, currentUser.getMemberType());
        }
        editor.apply();
    }
}