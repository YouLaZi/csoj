# 团体竞赛功能设计方案

> 版本：1.0
> 日期：2026-03-02
> 状态：设计阶段
> 注意：本功能需禁用机器人助手干扰

---

## 1. 功能概述

### 1.1 目标
为 CSOJ 平台添加团体竞赛功能，支持：
- 团队创建与管理
- 团队间竞赛对抗
- 团队积分排行榜
- 团队数据统计

### 1.2 用户角色

| 角色 | 权限 |
|------|------|
| 队长 | 创建队伍、邀请成员、设置战术、提交报名 |
| 副队长 | 管理成员、修改队伍信息 |
| 队员 | 参与竞赛、查看队内数据 |
| 游客 | 查看公开竞赛和排行榜 |

---

## 2. 数据库设计

### 2.1 团队表 (team)

```sql
CREATE TABLE IF NOT EXISTS `team` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '队伍ID',
    `name` varchar(100) NOT NULL COMMENT '队伍名称',
    `description` text COMMENT '队伍简介',
    `avatar` varchar(512) COMMENT '队伍头像URL',
    `max_members` int DEFAULT 5 COMMENT '最大成员数',
    `is_public` tinyint DEFAULT 1 COMMENT '是否公开招募',
    `invite_code` varchar(32) COMMENT '邀请码',
    `leader_id` bigint NOT NULL COMMENT '队长用户ID',
    `total_score` bigint DEFAULT 0 COMMENT '团队总积分',
    `win_count` int DEFAULT 0 COMMENT '胜场数',
    `lose_count` int DEFAULT 0 COMMENT '负场数',
    `rating` int DEFAULT 1000 COMMENT 'ELO积分',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete` tinyint DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_name` (`name`),
    INDEX `idx_leader` (`leader_id`),
    INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队表';
```

### 2.2 团队成员表 (team_member)

```sql
CREATE TABLE IF NOT EXISTS `team_member` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `team_id` bigint NOT NULL COMMENT '团队ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role` varchar(20) DEFAULT 'member' COMMENT '角色：leader/vice_leader/member',
    `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `contribution_score` int DEFAULT 0 COMMENT '贡献积分',
    `is_delete` tinyint DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_team_user` (`team_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';
```

### 2.3 竞赛表 (competition)

```sql
CREATE TABLE IF NOT EXISTS `competition` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(200) NOT NULL COMMENT '竞赛标题',
    `description` text COMMENT '竞赛描述',
    `type` varchar(50) NOT NULL COMMENT '竞赛类型',
    `status` varchar(20) DEFAULT 'upcoming' COMMENT '状态：upcoming/ongoing/ended',
    `max_teams` int DEFAULT 16 COMMENT '最大参赛队伍数',
    `team_size` int DEFAULT 3 COMMENT '每队人数',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `register_start` datetime COMMENT '报名开始时间',
    `register_end` datetime COMMENT '报名截止时间',
    `rules` text COMMENT '竞赛规则(JSON)',
    `prizes` text COMMENT '奖品设置(JSON)',
    `organizer_id` bigint COMMENT '主办方ID',
    `is_public` tinyint DEFAULT 1 COMMENT '是否公开',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete` tinyint DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';
```

### 2.4 竞赛报名表 (competition_registration)

```sql
CREATE TABLE IF NOT EXISTS `competition_registration` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `competition_id` bigint NOT NULL COMMENT '竞赛ID',
    `team_id` bigint NOT NULL COMMENT '团队ID',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
    `register_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `seed` int COMMENT '种子排名',
    `is_delete` tinyint DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_comp_team` (`competition_id`, `team_id`),
    INDEX `idx_team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛报名表';
```

### 2.5 竞赛对局表 (competition_match)

```sql
CREATE TABLE IF NOT EXISTS `competition_match` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `competition_id` bigint NOT NULL COMMENT '竞赛ID',
    `round` int NOT NULL COMMENT '轮次',
    `match_number` int NOT NULL COMMENT '场次',
    `team_a_id` bigint NOT NULL COMMENT 'A队ID',
    `team_b_id` bigint NOT NULL COMMENT 'B队ID',
    `winner_id` bigint COMMENT '获胜队ID',
    `score_a` int DEFAULT 0 COMMENT 'A队得分',
    `score_b` int DEFAULT 0 COMMENT 'B队得分',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/ongoing/completed',
    `scheduled_time` datetime COMMENT '预定时间',
    `start_time` datetime COMMENT '实际开始时间',
    `end_time` datetime COMMENT '结束时间',
    `questions` text COMMENT '题目列表(JSON)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `is_delete` tinyint DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_competition` (`competition_id`),
    INDEX `idx_teams` (`team_a_id`, `team_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛对局表';
```

### 2.6 对局参赛记录表 (match_participation)

```sql
CREATE TABLE IF NOT EXISTS `match_participation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `match_id` bigint NOT NULL COMMENT '对局ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `team_id` bigint NOT NULL COMMENT '所属团队ID',
    `question_id` bigint COMMENT '分配的题目ID',
    `submission_id` bigint COMMENT '提交记录ID',
    `score` int DEFAULT 0 COMMENT '得分',
    `time_used` int COMMENT '用时(秒)',
    `is_correct` tinyint DEFAULT 0 COMMENT '是否通过',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_match` (`match_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对局参赛记录表';
```

---

## 3. 竞赛类型设计

### 3.1 竞赛类型枚举

```java
public enum CompetitionType {
    ELIMINATION("elimination", "淘汰赛"),        // 单败淘汰
    DOUBLE_ELIMINATION("double_elimination", "双败淘汰"),
    ROUND_ROBIN("round_robin", "循环赛"),       // 每队互战
    SWISS("swiss", "瑞士轮"),                   // 积分匹配
    TEAM_AC("team_ac", "团队AC赛"),             // 团队AC数比拼
    RELAY("relay", "接力赛"),                   // 轮流答题
    BATTLE("battle", "对抗赛");                 // 同时答题比拼
}
```

### 3.2 各类型规则说明

#### 3.2.1 淘汰赛 (Elimination)

```
结构示例（8队）：
    第一轮        半决赛         决赛
    ┌─────┐
    │ T1  ├───┐
    └─────┘   │   ┌─────┐
    ┌─────┐   ├───│ W1  ├───┐
    │ T2  ├───┘   └─────┘   │   ┌─────┐
    └─────┘                 │   │     │
    ┌─────┐                 ├───│冠军 │
    │ T3  ├───┐   ┌─────┐   │   │     │
    └─────┘   │   │ W2  ├───┘   └─────┘
    ┌─────┐   ├───│     │
    │ T4  ├───┘   └─────┘
    └─────┘
    ... (对称下半区)
```

- 单场定胜负，败者淘汰
- 适用于时间有限的竞赛
- 需要种子排名避免强队过早相遇

#### 3.2.2 团队AC赛 (Team AC)

```
规则：
- 每队 3-5 人
- 比赛时长 2-5 小时
- 所有队员共享题目池
- 按团队总AC数排名

计分：
- AC数 > 总用时 > 最后AC时间
- 每题首次AC有额外加分
```

#### 3.2.3 对抗赛 (Battle)

```
规则：
- 两队同时进行
- 每队派出相同人数
- 题目相同，限时作答
- 每题胜者得1分

流程：
┌─────────────────────────────────────┐
│          对抗赛流程                   │
├─────────────────────────────────────┤
│  1. 系统公布题目                      │
│  2. 双方同时开始作答（10分钟）          │
│  3. 先AC或得分高者获胜                 │
│  4. 下一题继续                         │
│  5. 总分高者赢得对局                   │
└─────────────────────────────────────┘
```

#### 3.2.4 接力赛 (Relay)

```
规则：
- 队员按顺序答题
- 前一题AC后下一人才能开始
- 中间可以换人（消耗换人次数）
- 总时间最短者获胜

流程：
T1: [队员A] ──AC──► [队员B] ──AC──► [队员C] ──AC──► 完成!
     │                │                │
   题目1            题目2            题目3
```

---

## 4. 后端接口设计

### 4.1 团队管理 API

```java
@RestController
@RequestMapping("/api/team")
public class TeamController {

    // 创建团队
    @PostMapping("/create")
    public BaseResponse<Long> createTeam(@RequestBody TeamCreateRequest request);

    // 更新团队信息
    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest request);

    // 解散团队
    @DeleteMapping("/{teamId}")
    public BaseResponse<Boolean> deleteTeam(@PathVariable Long teamId);

    // 获取团队详情
    @GetMapping("/{teamId}")
    public BaseResponse<TeamVO> getTeam(@PathVariable Long teamId);

    // 获取团队列表
    @GetMapping("/list")
    public BaseResponse<Page<TeamVO>> listTeams(TeamQueryRequest request);

    // 邀请成员（生成邀请码）
    @PostMapping("/{teamId}/invite")
    public BaseResponse<String> generateInviteCode(@PathVariable Long teamId);

    // 通过邀请码加入
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest request);

    // 退出团队
    @PostMapping("/{teamId}/quit")
    public BaseResponse<Boolean> quitTeam(@PathVariable Long teamId);

    // 移除成员
    @PostMapping("/{teamId}/kick")
    public BaseResponse<Boolean> kickMember(@RequestBody KickRequest request);

    // 转让队长
    @PostMapping("/{teamId}/transfer")
    public BaseResponse<Boolean> transferLeader(@RequestBody TransferRequest request);

    // 获取我的团队
    @GetMapping("/my")
    public BaseResponse<List<TeamVO>> getMyTeams();

    // 团队排行榜
    @GetMapping("/ranking")
    public BaseResponse<Page<TeamRankVO>> getTeamRanking(RankingQueryRequest request);
}
```

### 4.2 竞赛管理 API

```java
@RestController
@RequestMapping("/api/competition")
public class CompetitionController {

    // 创建竞赛（管理员）
    @PostMapping("/create")
    public BaseResponse<Long> createCompetition(@RequestBody CompetitionCreateRequest request);

    // 更新竞赛
    @PutMapping("/update")
    public BaseResponse<Boolean> updateCompetition(@RequestBody CompetitionUpdateRequest request);

    // 获取竞赛详情
    @GetMapping("/{competitionId}")
    public BaseResponse<CompetitionVO> getCompetition(@PathVariable Long competitionId);

    // 竞赛列表
    @GetMapping("/list")
    public BaseResponse<Page<CompetitionVO>> listCompetitions(CompetitionQueryRequest request);

    // 报名竞赛
    @PostMapping("/{competitionId}/register")
    public BaseResponse<Boolean> registerCompetition(@PathVariable Long competitionId,
                                                      @RequestBody RegisterRequest request);

    // 取消报名
    @PostMapping("/{competitionId}/cancel")
    public BaseResponse<Boolean> cancelRegistration(@PathVariable Long competitionId);

    // 获取参赛队伍
    @GetMapping("/{competitionId}/teams")
    public BaseResponse<List<TeamVO>> getCompetitionTeams(@PathVariable Long competitionId);

    // 获取赛程
    @GetMapping("/{competitionId}/schedule")
    public BaseResponse<List<MatchVO>> getCompetitionSchedule(@PathVariable Long competitionId);

    // 获取对局详情
    @GetMapping("/match/{matchId}")
    public BaseResponse<MatchDetailVO> getMatchDetail(@PathVariable Long matchId);

    // 开始对局
    @PostMapping("/match/{matchId}/start")
    public BaseResponse<Boolean> startMatch(@PathVariable Long matchId);

    // 提交答案（对局中）
    @PostMapping("/match/{matchId}/submit")
    public BaseResponse<SubmitResultVO> submitInMatch(@PathVariable Long matchId,
                                                       @RequestBody MatchSubmitRequest request);

    // 结束对局
    @PostMapping("/match/{matchId}/end")
    public BaseResponse<MatchResultVO> endMatch(@PathVariable Long matchId);

    // 获取竞赛排行榜
    @GetMapping("/{competitionId}/ranking")
    public BaseResponse<List<CompetitionRankVO>> getCompetitionRanking(@PathVariable Long competitionId);

    // 获取团队战绩
    @GetMapping("/team/{teamId}/history")
    public BaseResponse<Page<MatchHistoryVO>> getTeamMatchHistory(@PathVariable Long teamId);
}
```

---

## 5. 前端页面设计

### 5.1 页面结构

```
/competition
├── /team                    # 团队中心
│   ├── /list               # 团队列表
│   ├── /create             # 创建团队
│   ├── /:teamId            # 团队详情
│   │   ├── /members        # 成员管理
│   │   ├── /statistics     # 数据统计
│   │   └── /history        # 比赛记录
│   └── /join               # 加入团队（邀请码）
│
├── /contest                 # 竞赛中心
│   ├── /list               # 竞赛列表
│   ├── /:competitionId     # 竞赛详情
│   │   ├── /register       # 报名页面
│   │   ├── /schedule       # 赛程表
│   │   ├── /ranking        # 排行榜
│   │   └── /rules          # 规则说明
│   └── /create             # 创建竞赛（管理员）
│
└── /match                   # 对局
    └── /:matchId            # 对局进行中
        ├── /lobby           # 等候室
        ├── /battle          # 对战界面
        └── /result          # 结果页面
```

### 5.2 核心页面设计

#### 5.2.1 团队详情页

```
┌─────────────────────────────────────────────────────────────┐
│  [团队头像]  团队名称                          [编辑] [邀请]  │
│             ★★★★☆  排名: #15  积分: 1250                    │
├─────────────────────────────────────────────────────────────┤
│  [队员列表]  [数据统计]  [比赛记录]                          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐          │
│  │ 头像 │  │ 头像 │  │ 头像 │  │ 头像 │  │  +   │          │
│  │ 队长 │  │副队长│  │ 队员 │  │ 队员 │  │ 邀请 │          │
│  │ 1250 │  │ 1180 │  │ 950  │  │ 890  │  │      │          │
│  └──────┘  └──────┘  └──────┘  └──────┘  └──────┘          │
├─────────────────────────────────────────────────────────────┤
│  近期战绩                                                    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ VS 团队B  ● 胜利  3:1  2026-03-01                    │    │
│  │ VS 团队C  ○ 失败  2:3  2026-02-28                    │    │
│  │ VS 团队D  ● 胜利  4:0  2026-02-27                    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

#### 5.2.2 对战界面

```
┌─────────────────────────────────────────────────────────────┐
│  团队竞赛 - 第一轮                            00:15:30      │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐   VS   ┌─────────────────┐             │
│  │    团队 A       │        │    团队 B       │             │
│  │    ●●●○○       │        │    ●●○○○       │             │
│  │     3:2        │        │                 │             │
│  └─────────────────┘        └─────────────────┘             │
├─────────────────────────────────────────────────────────────┤
│  当前题目 #3 (中等)                                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ 题目标题: 两数之和                                   │    │
│  │ 时间限制: 10分钟                                    │    │
│  │                                                     │    │
│  │ [题目描述区域]                                       │    │
│  │                                                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ [代码编辑器]                                        │    │
│  │                                                     │    │
│  │                                                     │    │
│  │                                                     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│              [提交答案]    [跳过此题]                        │
└─────────────────────────────────────────────────────────────┘
```

#### 5.2.3 赛程表（淘汰赛）

```
┌─────────────────────────────────────────────────────────────┐
│  竞赛赛程 - CSOJ 春季赛                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   第一轮          半决赛           决赛                      │
│                                                             │
│   ┌──────┐                                          冠军    │
│   │团队A │───┐                                              │
│   └──────┘   │   ┌──────┐                                  │
│              ├───│团队A │───┐                              │
│   ┌──────┐   │   └──────┘   │   ┌──────┐                  │
│   │团队B │───┘              ├───│     │                  │
│   └──────┘                  │   │     │                  │
│                             │   │     │                  │
│   ┌──────┐                  │   │     │                  │
│   │团队C │───┐   ┌──────┐   │   └──────┘                  │
│   └──────┘   ├───│团队C │───┘                              │
│              │   └──────┘                                  │
│   ┌──────┐   │                                             │
│   │团队D │───┘                                             │
│   └──────┘                                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. 核心业务逻辑

### 6.1 淘汰赛对阵生成

```java
@Service
public class EliminationMatchGenerator {

    /**
     * 生成淘汰赛对阵表
     * @param teamIds 参赛队伍ID列表（必须是2的幂次）
     * @param seedRankings 种子排名
     */
    public List<CompetitionMatch> generateMatches(Long competitionId,
                                                   List<Long> teamIds,
                                                   Map<Long, Integer> seedRankings) {
        int teamCount = teamIds.size();
        if (!isPowerOfTwo(teamCount)) {
            // 补充轮空队伍
            teamIds = padToPowerOfTwo(teamIds);
            teamCount = teamIds.size();
        }

        // 按种子排名排序
        teamIds.sort((a, b) -> seedRankings.getOrDefault(a, 999)
                         - seedRankings.getOrDefault(b, 999));

        // 使用蛇形排列避免种子过早相遇
        List<Long> seededOrder = snakeSeeding(teamIds);

        // 生成第一轮对阵
        int totalRounds = (int) (Math.log(teamCount) / Math.log(2));
        List<CompetitionMatch> matches = new ArrayList<>();

        for (int i = 0; i < teamCount / 2; i++) {
            CompetitionMatch match = new CompetitionMatch();
            match.setCompetitionId(competitionId);
            match.setRound(1);
            match.setMatchNumber(i + 1);
            match.setTeamAId(seededOrder.get(i * 2));
            match.setTeamBId(seededOrder.get(i * 2 + 1));
            match.setStatus("pending");
            matches.add(match);
        }

        // 生成后续轮次的空对阵
        for (int round = 2; round <= totalRounds; round++) {
            int matchCount = teamCount / (int) Math.pow(2, round);
            for (int i = 0; i < matchCount; i++) {
                CompetitionMatch match = new CompetitionMatch();
                match.setCompetitionId(competitionId);
                match.setRound(round);
                match.setMatchNumber(i + 1);
                match.setStatus("pending");
                matches.add(match);
            }
        }

        return matches;
    }

    /**
     * 蛇形种子排列
     * 种子排名: 1,2,3,4,5,6,7,8
     * 排列结果: 1,8,4,5,2,7,3,6
     * 保证种子1和2在决赛前不会相遇
     */
    private List<Long> snakeSeeding(List<Long> sortedTeams) {
        if (sortedTeams.size() <= 2) return sortedTeams;

        List<Long> result = new ArrayList<>();
        int n = sortedTeams.size();

        // 递归分割
        List<Long> top = sortedTeams.subList(0, n / 2);
        List<Long> bottom = sortedTeams.subList(n / 2, n);

        // 蛇形排列
        result.addAll(snakeSeeding(top));
        Collections.reverse(bottom);
        result.addAll(snakeSeeding(bottom));

        return result;
    }
}
```

### 6.2 ELO 积分计算

```java
@Service
public class TeamRatingService {

    private static final int K = 32; // K因子
    private static final int INITIAL_RATING = 1000;

    /**
     * 更新双方团队积分
     */
    public void updateRatings(Long teamAId, Long teamBId, int scoreA, int scoreB) {
        Team teamA = teamMapper.selectById(teamAId);
        Team teamB = teamMapper.selectById(teamBId);

        int ratingA = teamA.getRating();
        int ratingB = teamB.getRating();

        // 计算期望胜率
        double expectedA = 1.0 / (1.0 + Math.pow(10, (ratingB - ratingA) / 400.0));
        double expectedB = 1.0 - expectedA;

        // 确定实际得分
        double actualA = scoreA > scoreB ? 1.0 : (scoreA < scoreB ? 0.0 : 0.5);
        double actualB = 1.0 - actualA;

        // 计算新积分
        int newRatingA = (int) (ratingA + K * (actualA - expectedA));
        int newRatingB = (int) (ratingB + K * (actualB - expectedB));

        // 更新数据库
        teamA.setRating(newRatingA);
        teamB.setRating(newRatingB);

        if (scoreA > scoreB) {
            teamA.setWinCount(teamA.getWinCount() + 1);
            teamB.setLoseCount(teamB.getLoseCount() + 1);
        } else if (scoreA < scoreB) {
            teamB.setWinCount(teamB.getWinCount() + 1);
            teamA.setLoseCount(teamA.getLoseCount() + 1);
        }

        teamMapper.updateById(teamA);
        teamMapper.updateById(teamB);
    }

    /**
     * 获取排名
     */
    public int getRanking(int rating) {
        return teamMapper.countByRatingGreaterThan(rating) + 1;
    }
}
```

### 6.3 实时对战状态同步

```java
@Service
public class MatchRealtimeService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 广播对局状态
     */
    public void broadcastMatchState(Long matchId, MatchState state) {
        messagingTemplate.convertAndSend(
            "/topic/match/" + matchId,
            MatchStateMessage.builder()
                .matchId(matchId)
                .scoreA(state.getScoreA())
                .scoreB(state.getScoreB())
                .currentQuestion(state.getCurrentQuestion())
                .timeRemaining(state.getTimeRemaining())
                .build()
        );
    }

    /**
     * 通知队员提交结果
     */
    public void notifySubmission(Long matchId, Long teamId, SubmitResult result) {
        messagingTemplate.convertAndSend(
            "/topic/match/" + matchId + "/team/" + teamId,
            SubmissionMessage.builder()
                .userId(result.getUserId())
                .questionId(result.getQuestionId())
                .isCorrect(result.isCorrect())
                .timeUsed(result.getTimeUsed())
                .build()
        );
    }

    /**
     * 对局结束通知
     */
    public void notifyMatchEnd(Long matchId, MatchResult result) {
        messagingTemplate.convertAndSend(
            "/topic/match/" + matchId + "/end",
            MatchEndMessage.builder()
                .winnerId(result.getWinnerId())
                .finalScoreA(result.getScoreA())
                .finalScoreB(result.getScoreB())
                .mvpUserId(result.getMvpUserId())
                .build()
        );
    }
}
```

---

## 7. WebSocket 消息设计

### 7.1 消息类型

```java
public enum MessageType {
    // 对局状态
    MATCH_START,        // 对局开始
    MATCH_STATE,        // 状态更新
    MATCH_END,          // 对局结束

    // 题目相关
    QUESTION_PUBLISH,   // 题目公布
    QUESTION_SUBMIT,    // 提交答案
    QUESTION_RESULT,    // 答题结果

    // 团队相关
    TEAM_SCORE_UPDATE,  // 分数更新
    MEMBER_JOIN,        // 成员加入
    MEMBER_LEAVE,       // 成员离开

    // 系统消息
    SYSTEM_NOTICE,      // 系统通知
    TIME_WARNING,       // 时间警告
    COUNTDOWN           // 倒计时
}
```

### 7.2 前端订阅

```javascript
// 对局 WebSocket 连接
const matchSocket = {
    // 连接到对局
    connect(matchId) {
        this.stompClient = Stomp.over(new SockJS('/ws/match'));
        this.stompClient.connect({}, (frame) => {
            // 订阅对局状态
            this.stompClient.subscribe(`/topic/match/${matchId}`, (message) => {
                this.handleMatchState(JSON.parse(message.body));
            });

            // 订阅团队私有消息
            this.stompClient.subscribe(`/user/queue/match/${matchId}`, (message) => {
                this.handlePrivateMessage(JSON.parse(message.body));
            });
        });
    },

    // 提交答案
    submitAnswer(matchId, questionId, code) {
        this.stompClient.send(`/app/match/${matchId}/submit`, {}, JSON.stringify({
            questionId,
            code,
            timestamp: Date.now()
        }));
    }
};
```

---

## 8. 安全与防作弊

### 8.1 防作弊措施

| 风险 | 措施 |
|------|------|
| 多账号 | 同一设备/IP 限制参赛数量 |
| 代码抄袭 | 代码相似度检测，人工复核 |
| 外部帮助 | 禁止复制粘贴，限制网络访问 |
| 提前获取题目 | 题目加密，比赛开始时解密 |
| 时间作弊 | 服务端时间校验，防止客户端篡改 |

### 8.2 权限控制

```java
@Aspect
@Component
public class CompetitionAccessAspect {

    @Before("@annotation(competitionAccess)")
    public void checkAccess(JoinPoint joinPoint, CompetitionAccess competitionAccess) {
        Long competitionId = extractCompetitionId(joinPoint);
        Long userId = getCurrentUserId();

        Competition competition = competitionService.getById(competitionId);

        // 检查竞赛状态
        if (competitionAccess.requireOngoing() &&
            !"ongoing".equals(competition.getStatus())) {
            throw new BusinessException("竞赛未开始或已结束");
        }

        // 检查参赛资格
        if (competitionAccess.requireParticipant()) {
            if (!competitionService.isParticipant(competitionId, userId)) {
                throw new BusinessException("您未报名此竞赛");
            }
        }

        // 检查管理员权限
        if (competitionAccess.requireAdmin()) {
            if (!userService.isAdmin(userId)) {
                throw new BusinessException("需要管理员权限");
            }
        }
    }
}
```

---

## 9. 实施计划

### Phase 1：基础功能（5-7天）

- [ ] 数据库表创建
- [ ] 团队管理 API（创建、加入、退出）
- [ ] 团队详情页面
- [ ] 团队列表页面

### Phase 2：竞赛管理（5-7天）

- [ ] 竞赛创建 API
- [ ] 报名功能
- [ ] 淘汰赛对阵生成
- [ ] 竞赛详情页面

### Phase 3：实时对战（7-10天）

- [ ] WebSocket 集成
- [ ] 对战界面
- [ ] 实时计分
- [ ] 对局结束处理

### Phase 4：排行榜与统计（3-5天）

- [ ] ELO 积分系统
- [ ] 团队排行榜
- [ ] 竞赛排行榜
- [ ] 数据统计页面

### Phase 5：优化与测试（3-5天）

- [ ] 性能优化
- [ ] 防作弊机制
- [ ] 压力测试
- [ ] 文档完善

---

## 10. 技术依赖

| 组件 | 用途 |
|------|------|
| WebSocket (STOMP) | 实时对战通信 |
| Redis | 在线状态、实时数据缓存 |
| Spring Scheduler | 定时任务（竞赛开始/结束） |
| RabbitMQ | 异步消息处理 |

---

## 11. 预期效果

### 用户体验
- 增强社区互动性
- 提高用户粘性
- 促进学习交流

### 平台价值
- 差异化竞争优势
- 潜在商业化机会
- 数据积累价值

---

## 12. 注意事项

1. **禁用机器人助手**：竞赛期间 ChatBot 组件自动隐藏
2. **网络延迟**：考虑高延迟场景的公平性
3. **并发控制**：大规模竞赛的并发处理
4. **数据备份**：竞赛数据的重要性
