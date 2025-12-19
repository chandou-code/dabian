#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园失物招领系统API快速测试脚本
轻量级版本，快速验证核心功能
"""

import requests
import json

# 配置
BASE_URL = "http://localhost:18080/api"

def test_api():
    """快速测试API核心功能"""
    print("🚀 开始快速API测试")
    print("-" * 40)
    
    # 1. 健康检查
    print("1. 测试系统健康...")
    try:
        response = requests.get(f"{BASE_URL}/test/health")
        if response.status_code == 200:
            print("✅ 系统健康检查通过")
        else:
            print(f"❌ 健康检查失败: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 连接失败: {e}")
        return False
    
    # 2. 用户登录
    print("\n2. 测试用户登录...")
    login_data = {
        "username": "admin",
        "password": "123456"
    }
    
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=login_data)
        if response.status_code == 200:
            data = response.json()
            if data.get('code') == 200:
                token = data['data']['token']
                user_info = data['data']['user']
                print(f"✅ 登录成功 - 用户: {user_info['username']}, 角色: {user_info['role']}")
                headers = {'Authorization': f'Bearer {token}'}
            else:
                print(f"❌ 登录失败: {data.get('message')}")
                return False
        else:
            print(f"❌ 登录请求失败: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 登录请求异常: {e}")
        return False
    
    # 3. 获取物品列表
    print("\n3. 测试获取物品列表...")
    try:
        response = requests.get(f"{BASE_URL}/items/lost-items", params={"current": 1, "pageSize": 5})
        if response.status_code == 200:
            data = response.json()
            if data.get('code') == 200:
                items = data.get('data', {}).get('list', [])
                print(f"✅ 获取物品列表成功 - 共 {len(items)} 个物品")
            else:
                print(f"❌ 获取列表失败: {data.get('message')}")
        else:
            print(f"❌ 请求失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 请求异常: {e}")
    
    # 4. 搜索测试
    print("\n4. 测试物品搜索...")
    try:
        response = requests.get(f"{BASE_URL}/items/search", 
                               params={"q": "手机", "type": "all", "current": 1, "pageSize": 5})
        if response.status_code == 200:
            data = response.json()
            if data.get('code') == 200:
                results = data.get('data', {}).get('results', [])
                total = data.get('data', {}).get('total', 0)
                print(f"✅ 搜索成功 - 找到 {total} 个结果")
            else:
                print(f"❌ 搜索失败: {data.get('message')}")
        else:
            print(f"❌ 搜索请求失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 搜索请求异常: {e}")
    
    # 5. 用户统计
    print("\n5. 测试用户统计...")
    try:
        response = requests.get(f"{BASE_URL}/items/stats/user", headers=headers)
        if response.status_code == 200:
            data = response.json()
            if data.get('code') == 200:
                stats = data.get('data', {})
                print("✅ 获取用户统计成功")
                print(f"   统计数据: {json.dumps(stats, ensure_ascii=False, indent=6)}")
            else:
                print(f"❌ 获取统计失败: {data.get('message')}")
        else:
            print(f"❌ 统计请求失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 统计请求异常: {e}")
    
    print("\n" + "-" * 40)
    print("🎉 快速测试完成！")
    print("如需完整测试，请运行 test_api_intelligent.py")

if __name__ == "__main__":
    test_api()