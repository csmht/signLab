#!/bin/bash

# 飞书通知脚本
# 用于在 GitHub Actions 中发送飞书通知

set -e

# 从环境变量获取参数
EVENT_NAME="${GITHUB_EVENT_NAME}"
REPOSITORY="${GITHUB_REPOSITORY}"
SERVER_URL="${GITHUB_SERVER_URL}"
ACTOR="${GITHUB_ACTOR}"

echo "事件类型: ${EVENT_NAME}"
echo "仓库: ${REPOSITORY}"

# 判断事件类型并构建消息
if [ "${EVENT_NAME}" == "push" ]; then
  # Push 事件
  EVENT_TYPE="代码推送"
  BRANCH="${GITHUB_REF_NAME}"
  COMMIT_SHA="${GITHUB_SHA}"
  COMMIT_MESSAGE=$(git log -1 --pretty=format:"%s")
  AUTHOR="${ACTOR}"
  COMMIT_URL="${SERVER_URL}/${REPOSITORY}/commit/${COMMIT_SHA}"

  MESSAGE="🚀 **${EVENT_TYPE}通知**

📦 **仓库**: ${REPOSITORY}
🌿 **分支**: ${BRANCH}
👤 **操作人**: ${AUTHOR}
🔗 **提交**: ${COMMIT_SHA}
📝 **提交信息**: ${COMMIT_MESSAGE}
🔗 **查看详情**: ${COMMIT_URL}"

elif [ "${EVENT_NAME}" == "pull_request" ]; then
  # PR 事件 - 从 GITHUB_EVENT_PATH 文件中提取数据
  PR_ACTION=$(jq -r '.action' "$GITHUB_EVENT_PATH")
  PR_NUMBER=$(jq -r '.pull_request.number' "$GITHUB_EVENT_PATH")
  PR_TITLE=$(jq -r '.pull_request.title' "$GITHUB_EVENT_PATH")
  PR_AUTHOR=$(jq -r '.pull_request.user.login' "$GITHUB_EVENT_PATH")
  PR_URL=$(jq -r '.pull_request.html_url' "$GITHUB_EVENT_PATH")
  PR_BRANCH=$(jq -r '.pull_request.head.ref' "$GITHUB_EVENT_PATH")
  PR_MERGED=$(jq -r '.pull_request.merged' "$GITHUB_EVENT_PATH")

  if [ "${PR_ACTION}" == "opened" ]; then
    EVENT_TYPE="PR 提起"
    MESSAGE="📝 **${EVENT_TYPE}通知**

📦 **仓库**: ${REPOSITORY}
🔢 **PR编号**: #${PR_NUMBER}
📌 **PR标题**: ${PR_TITLE}
👤 **发起人**: ${PR_AUTHOR}
🌿 **源分支**: ${PR_BRANCH}
🎯 **目标分支**: master
🔗 **查看详情**: ${PR_URL}"

  elif [ "${PR_ACTION}" == "synchronize" ]; then
    EVENT_TYPE="PR 更新"
    MESSAGE="🔄 **${EVENT_TYPE}通知**

📦 **仓库**: ${REPOSITORY}
🔢 **PR编号**: #${PR_NUMBER}
📌 **PR标题**: ${PR_TITLE}
👤 **更新人**: ${PR_AUTHOR}
🌿 **源分支**: ${PR_BRANCH}
🎯 **目标分支**: master
🔗 **查看详情**: ${PR_URL}"

  elif [ "${PR_ACTION}" == "closed" ]; then
    if [ "${PR_MERGED}" == "true" ]; then
      EVENT_TYPE="PR 合并"
      MESSAGE="✅ **${EVENT_TYPE}通知**

📦 **仓库**: ${REPOSITORY}
🔢 **PR编号**: #${PR_NUMBER}
📌 **PR标题**: ${PR_TITLE}
👤 **合并人**: ${PR_AUTHOR}
🌿 **源分支**: ${PR_BRANCH}
🎯 **目标分支**: master
🔗 **查看详情**: ${PR_URL}"
    else
      # PR 关闭但未合并
      EVENT_TYPE="PR 关闭"
      MESSAGE="❌ **${EVENT_TYPE}通知**

📦 **仓库**: ${REPOSITORY}
🔢 **PR编号**: #${PR_NUMBER}
📌 **PR标题**: ${PR_TITLE}
👤 **关闭人**: ${PR_AUTHOR}
🌿 **源分支**: ${PR_BRANCH}
🔗 **查看详情**: ${PR_URL}"
    fi
  fi
fi

echo "消息内容:"
echo "${MESSAGE}"

# 发送到飞书 webhook
echo "发送飞书通知..."

# 使用 jq 构建 JSON 请求，确保正确处理特殊字符
JSON_DATA=$(jq -n \
  --arg msg_type "text" \
  --arg text "${MESSAGE}" \
  '{
    msg_type: $msg_type,
    content: {
      text: $text
    }
  }')

curl -X POST "${FEISHU_WEBHOOK_URL}" \
  -H "Content-Type: application/json" \
  -d "${JSON_DATA}"

echo "飞书通知发送完成！"