import requests
import base64
import os
import re
import json

# 配置
API_KEY = "sk-xnscttiarytendwlcahuumvpqtvqpibbkipejzcfxjsmjuwa"
URL = "https://api.siliconflow.cn/v1/chat/completions"
IMAGE_PATH = "4.jpg"

# 检查图片是否存在
if not os.path.exists(IMAGE_PATH):
    print(f"错误：图片文件 {IMAGE_PATH} 不存在！")
    exit(1)

# 将图片转换为base64格式
def image_to_base64(image_path):
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    return f"data:image/jpeg;base64,{encoded_string}"

# 从AI返回的JSON中提取物品信息
def extract_item_info(json_content):
    # 首先处理带有代码块标记的情况
    if json_content.startswith('```json'):
        # 去除代码块标记
        json_content = json_content.replace('```json', '').replace('```', '').strip()
    elif json_content.startswith('```'):
        # 处理没有指定语言的代码块
        json_content = json_content.replace('```', '').strip()

    try:
        # 尝试直接解析JSON，忽略换行符和空格
        data = json.loads(json_content)
        return {
            "item": data.get("item", None),
            "detail": data.get("detail", None),
            "category": data.get("category", None)
        }
    except json.JSONDecodeError:
        # 如果直接解析失败，尝试用更宽松的正则表达式
        # 匹配包含item、detail和category的JSON对象，允许换行符和空格
        json_pattern = r'\{[\s\S]*?"item"[\s\S]*?"detail"[\s\S]*?"category"[\s\S]*?\}|\{[\s\S]*?"category"[\s\S]*?"item"[\s\S]*?"detail"[\s\S]*?\}'
        json_match = re.search(json_pattern, json_content)
        if json_match:
            try:
                data = json.loads(json_match.group())
                return {
                    "item": data.get("item", None),
                    "detail": data.get("detail", None),
                    "category": data.get("category", None)
                }
            except json.JSONDecodeError:
                pass

        # 尝试只提取item和detail字段
        item_pattern = r'"item"\s*:\s*"([^"]+)"'
        item_match = re.search(item_pattern, json_content)

        detail_pattern = r'"detail"\s*:\s*"([^"]+)"'
        detail_match = re.search(detail_pattern, json_content)

        # 尝试提取category字段
        category_pattern = r'"category"\s*:\s*"([^"]+)"'
        category_match = re.search(category_pattern, json_content)

        # 如果能提取到部分字段，返回部分结果
        return {
            "item": item_match.group(1) if item_match else None,
            "detail": detail_match.group(1) if detail_match else None,
            "category": category_match.group(1) if category_match else None
        }

# 准备请求参数
payload = {
    "model": "Qwen/Qwen3-Omni-30B-A3B-Instruct",
    "messages": [
        {
            "role": "system",
            "content": "你是一个专业的物品识别助手，用于失物招领场景。请严格按照JSON格式输出结果，包含三个字段：item（物品的具体名称）、detail（物品的特征细节）和category（物品分类）。分类必须从以下列表中选择：电子产品、钱包证件、书籍文具、生活用品、衣物饰品、体育用品、其他物品。"
        },
        {
            "role": "user",
            "content": [
                {
                    "type": "image_url",
                    "image_url": {
                        "url": image_to_base64(IMAGE_PATH),
                        "detail": "high"
                    }
                },
                {
                    "type": "text",
                    "text": "请识别图片中的物品，返回物品的具体名称、详细特征和分类，严格按照JSON格式输出，例如：{\"item\": \"笔记本电脑\", \"detail\": \"银色外壳，13英寸屏幕，背面有苹果标志\", \"category\": \"电子产品\"}。分类必须从以下列表中选择：电子产品、钱包证件、书籍文具、生活用品、衣物饰品、体育用品、其他物品。"
                }
            ]
        }
    ]
}

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

# 发送请求
print("正在发送请求...")
try:
    response = requests.post(URL, json=payload, headers=headers)
    response.raise_for_status()  # 检查请求是否成功

    # 处理响应
    result = response.json()
    print("\n响应结果：")
    print(f"模型: {result['model']}")
    print(f"生成时间: {result['created']}")
    print(f"系统指纹: {result['system_fingerprint']}")

    # 获取AI生成的内容
    ai_content = result['choices'][0]['message']['content']
    print("\nAI原始输出：")
    print(ai_content)

    # 提取物品信息
    item_info = extract_item_info(ai_content)
    print("\n提取的物品信息：")
    if item_info["item"]:
        print(f"物品名称：{item_info['item']}")
    else:
        print("未成功提取物品名称")

    if item_info["detail"]:
        print(f"物品细节：{item_info['detail']}")
    else:
        print("未成功提取物品细节")

    if item_info["category"]:
        print(f"物品分类：{item_info['category']}")
    else:
        print("未成功提取物品分类")
        # 尝试直接从文本中提取
        print("\n尝试从文本中直接提取：")
        # 使用正则匹配可能的物品信息
        text_matches = re.findall(r'[\u4e00-\u9fa5a-zA-Z0-9]+(?:[\u4e00-\u9fa5a-zA-Z0-9\s]+)?[\u4e00-\u9fa5a-zA-Z0-9]+', ai_content)
        if text_matches:
            print("可能的物品信息：", text_matches[0])
except requests.exceptions.RequestException as e:
    print(f"\n请求失败：")
    print(f"状态码：{e.response.status_code if hasattr(e, 'response') else '未知'}")
    print(f"错误信息：{e}")
    if hasattr(e, 'response'):
        print(f"响应内容：{e.response.text}")
