#!/bin/bash

# CSOJ 定时备份配置脚本
# 用于设置 crontab 定时任务

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  配置数据库自动备份定时任务${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-database.sh"

# 检查备份脚本是否存在
if [ ! -f "$BACKUP_SCRIPT" ]; then
    echo "错误: 找不到备份脚本 $BACKUP_SCRIPT"
    exit 1
fi

# 确保备份脚本可执行
chmod +x "$BACKUP_SCRIPT"

echo -e "${YELLOW}请选择备份频率:${NC}"
echo "  1) 每天凌晨 2 点"
echo "  2) 每 6 小时"
echo "  3) 每小时"
echo "  4) 自定义"
read -p "请输入选项 (1-4): " choice

case $choice in
    1)
        CRON_EXPR="0 2 * * *"
        DESCRIPTION="每天凌晨 2 点"
        ;;
    2)
        CRON_EXPR="0 */6 * * *"
        DESCRIPTION="每 6 小时"
        ;;
    3)
        CRON_EXPR="0 * * * *"
        DESCRIPTION="每小时"
        ;;
    4)
        read -p "请输入 cron 表达式 (分 时 日 月 周): " CRON_EXPR
        DESCRIPTION="自定义: $CRON_EXPR"
        ;;
    *)
        echo "无效选项"
        exit 1
        ;;
esac

echo ""
echo -e "${YELLOW}配置信息:${NC}"
echo "备份频率: $DESCRIPTION"
echo "Cron 表达式: $CRON_EXPR"
echo "备份脚本: $BACKUP_SCRIPT"

# 创建 crontab 条目
CRON_JOB="$CRON_EXPR $BACKUP_SCRIPT >> $SCRIPT_DIR/backup.log 2>&1"

echo ""
read -p "确定要添加定时任务吗？(yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "操作已取消"
    exit 0
fi

# 添加到 crontab
echo ""
echo -e "${GREEN}正在配置定时任务...${NC}"

# 检查是否已存在相同的定时任务
if crontab -l 2>/dev/null | grep -q "$BACKUP_SCRIPT"; then
    echo "警告: 已存在备份定时任务，将被覆盖"

    # 删除旧的定时任务
    crontab -l 2>/dev/null | grep -v "$BACKUP_SCRIPT" | crontab -
fi

# 添加新的定时任务
(crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -

echo -e "${GREEN}✓ 定时任务配置成功！${NC}"
echo ""
echo "当前定时任务列表:"
crontab -l | grep -E "backup-database|backup"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  配置完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "备份日志将保存到: $SCRIPT_DIR/backup.log"
echo ""
echo "查看备份日志: tail -f $SCRIPT_DIR/backup.log"
echo "编辑定时任务: crontab -e"
echo "删除定时任务: crontab -e (删除对应行)"
