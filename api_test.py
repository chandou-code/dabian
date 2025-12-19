#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园失物招领系统API测试脚本
测试地址: http://localhost:18080/api
"""

import requests
import json
import time
import random
from datetime import datetime, timedelta

# API基础配置
BASE_URL = "http://localhost:18080/api"
HEADERS = {
    "Content-Type": "application/json",
    "Accept": "application/json"
}

# 全局变量存储token
auth_token = None
test_user_id = None
test_item_id = None
test_admin_token = None

def print_section(title):
    """打印测试章节标题"""
    print(f"\n{'='*50}")
    print(f" {title}")
    print(f"{'='*50}")

def print_result(test_name, success, data=None):
    """打印测试结果"""
    status = "✓ 成功" if success else "✗ 失败"
    print(f"{test_name}: {status}")
    if data:
        try:
            print(f"  响应: {json.dumps(data, ensure_ascii=False, indent=2)}")
        except:
            print(f"  响应: {data}")

def make_request(method, endpoint, data=None, params=None, use_token=True, custom_headers=None):
    """发送HTTP请求"""
    url = f"{BASE_URL}{endpoint}"
    headers = HEADERS.copy()
    
    if custom_headers:
        headers.update(custom_headers)
    
    if use_token and auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"
    
    try:
        if method.upper() == "GET":
            response = requests.get(url, headers=headers, params=params)
        elif method.upper() == "POST":
            response = requests.post(url, headers=headers, json=data, params=params)
        elif method.upper() == "PUT":
            response = requests.put(url, headers=headers, json=data, params=params)
        elif method.upper() == "DELETE":
            response = requests.delete(url, headers=headers, params=params)
        else:
            return None, "不支持的HTTP方法"
        
        return response, None
    except Exception as e:
        return None, str(e)

# ==================== 用户认证测试 ====================
def test_user_register():
    """测试用户注册"""
    print_section("用户注册测试")
    
    # 测试正常注册
    register_data = {
        "username": f"test_user_{int(time.time())}",
        "password": "Test123456",
        "email": f"test_{int(time.time())}@example.com",
        "realName": "测试用户",
        "phone": "13800138000",
        "studentId": f"2021{random.randint(1000, 9999)}"
    }
    
    response, error = make_request("POST", "/auth/register", register_data, use_token=False)
    if error:
        print_result("用户注册", False, error)
        return None
    
    result = response.json()
    success = response.status_code == 200
    print_result("用户注册", success, result)
    
    if success and result.get("data"):
        global test_user_id
        test_user_id = result["data"].get("id")
    
    return register_data

def test_user_login(login_data=None):
    """测试用户登录"""
    print_section("用户登录测试")
    
    if not login_data:
        login_data = {
            "username": "admin",
            "password": "admin123"
        }
    
    response, error = make_request("POST", "/auth/login", login_data, use_token=False)
    if error:
        print_result("用户登录", False, error)
        return False, None, None
    
    try:
        result = response.json()
        success = response.status_code == 200 and result.get("success", False)
        print_result("用户登录", success, result)
        
        token = None
        user_info = None
        if success and result.get("data"):
            data = result["data"]
            if isinstance(data, dict):
                token = data.get("token")
                user_info = data.get("user")
        
        if success and token:
            global auth_token
            auth_token = token
            
        return success, token, user_info
    except Exception as e:
        print_result("解析登录响应", False, str(e))
        return False, None, None

def test_get_current_user():
    """测试获取当前用户信息"""
    print_section("获取当前用户信息")
    
    response, error = make_request("GET", "/auth/me")
    if error:
        print_result("获取当前用户", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取当前用户", success, result)
    return success

# ==================== 物品管理测试 ====================
def test_create_item():
    """测试创建失物招领信息"""
    print_section("创建失物招领信息")
    
    # 测试创建失物
    lost_item = {
        "name": "测试丢失物品",
        "description": "这是一个测试丢失的物品，黑色钱包，内有身份证和银行卡",
        "category": "钱包证件",
        "location": "图书馆二楼",
        "type": "LOST",
        "contactInfo": "13800138000",
        "images": ["test_image_1.jpg", "test_image_2.jpg"]
    }
    
    response, error = make_request("POST", "/items", lost_item)
    if error:
        print_result("创建失物信息", False, error)
        return None
    
    result = response.json()
    success = response.status_code == 200
    print_result("创建失物信息", success, result)
    
    if success and result.get("data", {}).get("id"):
        global test_item_id
        test_item_id = result["data"]["id"]
    
    time.sleep(1)  # 避免创建频率过高
    
    # 测试创建拾物
    found_item = {
        "name": "测试拾得物品",
        "description": "在食堂拾得的蓝色水杯",
        "category": "生活用品",
        "location": "食堂三楼",
        "type": "FOUND",
        "contactInfo": "13900139000",
        "images": ["test_image_3.jpg"]
    }
    
    response, error = make_request("POST", "/items", found_item)
    result = response.json() if not error else {"error": error}
    success = (response.status_code == 200) if not error else False
    print_result("创建拾物信息", success, result)
    
    return test_item_id

def test_get_items():
    """测试获取物品列表"""
    print_section("获取物品列表")
    
    # 获取所有物品
    response, error = make_request("GET", "/items")
    if error:
        print_result("获取物品列表", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取物品列表", success, result)
    
    # 按类型筛选
    response, error = make_request("GET", "/items?type=LOST")
    if not error:
        result = response.json()
        success = response.status_code == 200
        print_result("按类型筛选(失物)", success, result)
    
    # 按分类筛选
    response, error = make_request("GET", "/items?category=钱包证件")
    if not error:
        result = response.json()
        success = response.status_code == 200
        print_result("按分类筛选", success, result)
    
    # 搜索功能
    response, error = make_request("GET", "/items?keyword=钱包")
    if not error:
        result = response.json()
        success = response.status_code == 200
        print_result("搜索功能", success, result)
    
    return True

def test_get_item_detail():
    """测试获取物品详情"""
    print_section("获取物品详情")
    
    if not test_item_id:
        print("没有可测试的物品ID")
        return False
    
    response, error = make_request("GET", f"/items/{test_item_id}")
    if error:
        print_result("获取物品详情", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取物品详情", success, result)
    return success

def test_update_item():
    """测试更新物品信息"""
    print_section("更新物品信息")
    
    if not test_item_id:
        print("没有可测试的物品ID")
        return False
    
    update_data = {
        "name": "更新后的物品名称",
        "description": "更新后的物品描述信息",
        "contactInfo": "13700137000"
    }
    
    response, error = make_request("PUT", f"/items/{test_item_id}", update_data)
    if error:
        print_result("更新物品信息", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("更新物品信息", success, result)
    return success

# ==================== 匹配系统测试 ====================
def test_find_matches():
    """测试智能匹配"""
    print_section("智能匹配测试")
    
    if not test_item_id:
        print("没有可测试的物品ID")
        return False
    
    response, error = make_request("GET", f"/matches/{test_item_id}")
    if error:
        print_result("智能匹配", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("智能匹配", success, result)
    return success

def test_get_my_matches():
    """测试获取我的匹配"""
    print_section("获取我的匹配")
    
    response, error = make_request("GET", "/matches/my")
    if error:
        print_result("获取我的匹配", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取我的匹配", success, result)
    return success

# ==================== 系统配置测试 ====================
def test_get_system_config():
    """测试获取系统配置"""
    print_section("系统配置测试")
    
    # 获取单个配置
    response, error = make_request("GET", "/config/auto_approve_users")
    if error:
        print_result("获取单个配置", False, error)
    else:
        result = response.json()
        success = response.status_code == 200
        print_result("获取单个配置", success, result)
    
    # 获取所有配置
    response, error = make_request("GET", "/config")
    if error:
        print_result("获取所有配置", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取所有配置", success, result)
    return success

def test_get_categories():
    """测试获取分类列表"""
    print_section("分类列表测试")
    
    response, error = make_request("GET", "/config/categories")
    if error:
        print_result("获取分类列表", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取分类列表", success, result)
    return success

def test_get_locations():
    """测试获取地点列表"""
    print_section("地点列表测试")
    
    response, error = make_request("GET", "/config/locations")
    if error:
        print_result("获取地点列表", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取地点列表", success, result)
    return success

# ==================== 统计信息测试 ====================
def test_get_statistics():
    """测试获取统计信息"""
    print_section("统计信息测试")
    
    response, error = make_request("GET", "/statistics")
    if error:
        print_result("获取统计信息", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取统计信息", success, result)
    return success

# ==================== 公告系统测试 ====================
def test_get_announcements():
    """测试获取公告列表"""
    print_section("公告系统测试")
    
    response, error = make_request("GET", "/announcements")
    if error:
        print_result("获取公告列表", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("获取公告列表", success, result)
    return success

# ==================== 文件上传测试 ====================
def test_upload_file():
    """测试文件上传"""
    print_section("文件上传测试")
    
    # 创建一个测试文件
    test_file_content = "这是一个测试文件的内容"
    files = {
        'file': ('test.txt', test_file_content, 'text/plain')
    }
    
    headers = HEADERS.copy()
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"
    
    try:
        response = requests.post(f"{BASE_URL}/upload", headers=headers, files=files)
        result = response.json()
        success = response.status_code == 200
        print_result("文件上传", success, result)
        return success
    except Exception as e:
        print_result("文件上传", False, str(e))
        return False

# ==================== 管理员功能测试 ====================
def test_admin_login():
    """测试管理员登录"""
    print_section("管理员登录测试")
    
    admin_data = {
        "username": "admin",
        "password": "admin123"
    }
    
    response, error = make_request("POST", "/auth/login", admin_data, use_token=False)
    if error:
        print_result("管理员登录", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("管理员登录", success, result)
    
    if success and result.get("data", {}).get("token"):
        global test_admin_token
        test_admin_token = result["data"]["token"]
    
    return success

def test_admin_functions():
    """测试管理员功能"""
    print_section("管理员功能测试")
    
    if not test_admin_token:
        print("未获得管理员token，跳过管理员功能测试")
        return False
    
    # 临时使用管理员token
    original_token = auth_token
    auth_token = test_admin_token
    
    # 测试获取所有用户
    response, error = make_request("GET", "/admin/users")
    if not error:
        result = response.json()
        success = response.status_code == 200
        print_result("获取所有用户", success, result)
    
    # 测试获取系统统计
    response, error = make_request("GET", "/admin/statistics")
    if not error:
        result = response.json()
        success = response.status_code == 200
        print_result("获取系统统计", success, result)
    
    # 恢复原始token
    auth_token = original_token
    return True

# ==================== AI功能测试 ====================
def test_ai_chat():
    """测试AI聊天功能"""
    print_section("AI聊天功能测试")
    
    chat_data = {
        "message": "我丢失了一个黑色的钱包，应该怎么办？"
    }
    
    response, error = make_request("POST", "/ai/chat", chat_data)
    if error:
        print_result("AI聊天", False, error)
        return False
    
    result = response.json()
    success = response.status_code == 200
    print_result("AI聊天", success, result)
    return success

# ==================== 主测试函数 ====================
def run_all_tests():
    """运行所有测试"""
    print("校园失物招领系统 API 测试开始")
    print(f"测试地址: {BASE_URL}")
    
    # 用户认证测试
    register_data = test_user_register()
    test_user_login(register_data)
    test_get_current_user()
    
    # 管理员登录
    test_admin_login()
    
    # 物品管理测试
    test_create_item()
    test_get_items()
    test_get_item_detail()
    test_update_item()
    
    # 匹配系统测试
    test_find_matches()
    test_get_my_matches()
    
    # 系统配置测试
    test_get_system_config()
    test_get_categories()
    test_get_locations()
    
    # 统计信息测试
    test_get_statistics()
    
    # 公告系统测试
    test_get_announcements()
    
    # 文件上传测试
    test_upload_file()
    
    # 管理员功能测试
    test_admin_functions()
    
    # AI功能测试
    test_ai_chat()
    
    print_section("测试完成")
    print("所有API接口测试已完成！")
    print(f"请注意检查失败的测试项，可能需要：")
    print("1. 确认数据库连接正常")
    print("2. 检查相关数据是否存在")
    print("3. 验证权限配置是否正确")

# ==================== 快速测试函数 ====================
def quick_test():
    """快速测试核心功能"""
    print("快速API测试")
    print("=" * 30)
    
    # 1. 登录测试
    login_success, token, user_info = test_user_login()
    if login_success and token:
        print("✓ 用户登录成功")
        
        # 2. 获取物品列表
        if test_get_items():
            print("✓ 物品列表获取成功")
        
        # 3. 系统配置测试
        if test_get_system_config():
            print("✓ 系统配置获取成功")
        
        print("快速测试完成！")
    else:
        print("✗ 登录失败，请检查系统状态")
        print("尝试注册新用户进行测试...")
        
        # 尝试注册新用户
        register_data = test_user_register()
        if register_data:
            print("尝试登录新注册的用户...")
            login_data = {
                "username": register_data["username"],
                "password": register_data["password"]
            }
            login_success, token, user_info = test_user_login(login_data)
            if login_success:
                print("✓ 新用户登录成功")
                if test_get_items():
                    print("✓ 物品列表获取成功")
                print("快速测试完成！")
            else:
                print("✗ 新用户登录也失败")
        else:
            print("✗ 用户注册也失败，请检查数据库和系统配置")

# ==================== 使用说明 ====================
if __name__ == "__main__":
    print("""
    选择测试模式：
    1. 完整测试 (输入 1 或 run_all_tests())
    2. 快速测试 (输入 2 或 quick_test())
    
    直接调用函数：
    - run_all_tests()    # 运行所有测试
    - quick_test()       # 快速测试核心功能
    
    单独测试模块：
    - test_user_login()      # 用户登录测试
    - test_create_item()     # 创建物品测试
    - test_get_items()       # 获取物品列表测试
    - test_ai_chat()         # AI聊天测试
    等等...
    """)
    
    # 默认执行快速测试
    quick_test()