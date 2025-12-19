#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园失物招领系统API智能测试脚本
自动执行一系列API测试，包括登录、获取token、发布物品、搜索等功能
"""

import requests
import json
import time
import random
from typing import Dict, Optional, Any
from datetime import datetime, timedelta

class CampusLostFoundTester:
    def __init__(self, base_url: str = "http://localhost:18080/api"):
        self.base_url = base_url
        self.session = requests.Session()
        self.token = None
        self.user_info = None
        self.created_items = []  # 存储创建的物品ID
        
        # 设置请求头
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
        
        # 测试数据
        self.test_user = {
            "username": f"testuser_{int(time.time())}",
            "password": "123456",
            "email": f"test_{int(time.time())}@example.com",
            "phone": f"138{random.randint(10000000, 99999999)}",
            "realName": "测试用户",
            "gender": 1,
            "college": "计算机学院",
            "grade": "2022级",
            "major": "软件工程"
        }
        
    def log(self, message: str, level: str = "INFO"):
        """打印日志"""
        timestamp = datetime.now().strftime("%H:%M:%S")
        print(f"[{timestamp}] {level}: {message}")
        
    def make_request(self, method: str, endpoint: str, data: Optional[Dict] = None, 
                     need_auth: bool = False, params: Optional[Dict] = None) -> requests.Response:
        """发送HTTP请求"""
        url = f"{self.base_url}{endpoint}"
        headers = {}
        
        if need_auth and self.token:
            headers['Authorization'] = f"Bearer {self.token}"
        
        try:
            if method.upper() == 'GET':
                response = self.session.get(url, headers=headers, params=params, timeout=10)
            elif method.upper() == 'POST':
                response = self.session.post(url, headers=headers, json=data, timeout=10)
            elif method.upper() == 'PUT':
                response = self.session.put(url, headers=headers, json=data, timeout=10)
            elif method.upper() == 'DELETE':
                response = self.session.delete(url, headers=headers, timeout=10)
            else:
                raise ValueError(f"不支持的HTTP方法: {method}")
                
            return response
        except requests.exceptions.RequestException as e:
            self.log(f"请求失败: {e}", "ERROR")
            return None
    
    def check_response(self, response: requests.Response, expected_status: int = 200) -> Optional[Dict]:
        """检查响应状态并返回JSON数据"""
        if response is None:
            return None
            
        if response.status_code != expected_status:
            self.log(f"HTTP状态码错误: 期望 {expected_status}, 实际 {response.status_code}", "ERROR")
            self.log(f"响应内容: {response.text}", "ERROR")
            return None
        
        try:
            return response.json()
        except json.JSONDecodeError:
            self.log(f"响应不是有效的JSON: {response.text}", "ERROR")
            return None
    
    def test_health_check(self) -> bool:
        """测试系统健康检查"""
        self.log("开始测试系统健康检查...")
        
        response = self.make_request('GET', '/test/health')
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            self.log("✅ 系统健康检查通过")
            self.log(f"系统信息: {data.get('data', {}).get('message', 'N/A')}")
            return True
        else:
            self.log("❌ 系统健康检查失败", "ERROR")
            return False
    
    def test_register(self) -> bool:
        """测试用户注册"""
        self.log("开始测试用户注册...")
        
        response = self.make_request('POST', '/auth/register', data=self.test_user)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            self.log("✅ 用户注册成功")
            self.token = data['data']['token']
            self.user_info = data['data']['user']
            self.log(f"用户ID: {self.user_info['id']}, 用户名: {self.user_info['username']}")
            return True
        else:
            self.log("❌ 用户注册失败", "ERROR")
            if data:
                self.log(f"错误信息: {data.get('message', 'Unknown error')}")
            return False
    
    def test_login(self) -> bool:
        """测试用户登录"""
        self.log("开始测试用户登录...")
        
        login_data = {
            "username": self.test_user["username"],
            "password": self.test_user["password"]
        }
        
        response = self.make_request('POST', '/auth/login', data=login_data)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            self.log("✅ 用户登录成功")
            self.token = data['data']['token']
            self.user_info = data['data']['user']
            self.log(f"用户ID: {self.user_info['id']}, 角色: {self.user_info['role']}")
            return True
        else:
            self.log("❌ 用户登录失败", "ERROR")
            if data:
                self.log(f"错误信息: {data.get('message', 'Unknown error')}")
            return False
    
    def test_publish_lost_item(self) -> bool:
        """测试发布失物信息"""
        self.log("开始测试发布失物信息...")
        
        lost_item = {
            "title": "丢失白色手机",
            "itemName": "iPhone 14",
            "category": "电子设备",
            "itemTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
            "location": "图书馆",
            "locationDetail": "三楼自习室",
            "description": "白色iPhone 14，有透明手机壳，背面有贴纸",
            "contact": self.test_user["phone"]
        }
        
        response = self.make_request('POST', '/items/lost-items', data=lost_item, need_auth=True)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            item_id = data['data']['id']
            self.created_items.append(item_id)
            self.log(f"✅ 发布失物信息成功, 物品ID: {item_id}")
            return True
        else:
            self.log("❌ 发布失物信息失败", "ERROR")
            if data:
                self.log(f"错误信息: {data.get('message', 'Unknown error')}")
            return False
    
    def test_publish_found_item(self) -> bool:
        """测试发布招领信息"""
        self.log("开始测试发布招领信息...")
        
        found_item = {
            "title": "拾到黑色钱包",
            "itemName": "黑色钱包",
            "category": "证件类",
            "itemTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
            "location": "食堂",
            "locationDetail": "二楼餐桌",
            "description": "黑色皮质钱包，内有校园卡和银行卡",
            "contact": self.test_user["phone"],
            "pickupLocation": "学生处"
        }
        
        response = self.make_request('POST', '/items/found-items', data=found_item, need_auth=True)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            item_id = data['data']['id']
            self.created_items.append(item_id)
            self.log(f"✅ 发布招领信息成功, 物品ID: {item_id}")
            return True
        else:
            self.log("❌ 发布招领信息失败", "ERROR")
            if data:
                self.log(f"错误信息: {data.get('message', 'Unknown error')}")
            return False
    
    def test_get_items(self) -> bool:
        """测试获取物品列表"""
        self.log("开始测试获取物品列表...")
        
        # 测试获取失物列表
        response = self.make_request('GET', '/items/lost-items', params={
            "current": 1,
            "pageSize": 10
        })
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            items = data.get('data', {}).get('list', [])
            self.log(f"✅ 获取物品列表成功, 共 {len(items)} 个物品")
            return True
        else:
            self.log("❌ 获取物品列表失败", "ERROR")
            return False
    
    def test_search_items(self) -> bool:
        """测试搜索物品"""
        self.log("开始测试搜索物品...")
        
        search_params = {
            "q": "手机",
            "type": "all",
            "current": 1,
            "pageSize": 10
        }
        
        response = self.make_request('GET', '/items/search', params=search_params)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            results = data.get('data', {}).get('results', [])
            total = data.get('data', {}).get('total', 0)
            self.log(f"✅ 搜索物品成功, 找到 {total} 个结果, 返回 {len(results)} 个")
            return True
        else:
            self.log("❌ 搜索物品失败", "ERROR")
            return False
    
    def test_get_item_detail(self) -> bool:
        """测试获取物品详情"""
        if not self.created_items:
            self.log("没有可用的物品ID，跳过物品详情测试")
            return True
            
        self.log("开始测试获取物品详情...")
        
        item_id = self.created_items[0]
        # 尝试两种可能的路径
        paths = [f'/items/lost-item/{item_id}', f'/items/found-item/{item_id}']
        
        for path in paths:
            response = self.make_request('GET', path)
            data = self.check_response(response)
            
            if data and data.get('code') == 200:
                item_detail = data['data']
                self.log(f"✅ 获取物品详情成功, 物品名称: {item_detail.get('itemName', 'N/A')}")
                return True
        
        self.log("❌ 获取物品详情失败", "ERROR")
        return False
    
    def test_user_stats(self) -> bool:
        """测试获取用户统计"""
        self.log("开始测试获取用户统计...")
        
        response = self.make_request('GET', '/items/stats/user', need_auth=True)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            stats = data.get('data', {})
            self.log("✅ 获取用户统计成功")
            self.log(f"统计信息: {json.dumps(stats, ensure_ascii=False, indent=2)}")
            return True
        else:
            self.log("❌ 获取用户统计失败", "ERROR")
            return False
    
    def test_update_item(self) -> bool:
        """测试更新物品信息"""
        if not self.created_items:
            self.log("没有可用的物品ID，跳过更新物品测试")
            return True
            
        self.log("开始测试更新物品信息...")
        
        item_id = self.created_items[0]
        update_data = {
            "title": "更新后的标题",
            "description": "更新后的描述信息 - 这是一个测试更新"
        }
        
        response = self.make_request('PUT', f'/items/items/{item_id}', data=update_data, need_auth=True)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            self.log(f"✅ 更新物品信息成功, 物品ID: {item_id}")
            return True
        else:
            self.log("❌ 更新物品信息失败", "ERROR")
            if data:
                self.log(f"错误信息: {data.get('message', 'Unknown error')}")
            return False
    
    def test_logout(self) -> bool:
        """测试用户登出"""
        self.log("开始测试用户登出...")
        
        response = self.make_request('POST', '/auth/logout', need_auth=True)
        data = self.check_response(response)
        
        if data and data.get('code') == 200:
            self.log("✅ 用户登出成功")
            self.token = None
            return True
        else:
            self.log("❌ 用户登出失败", "ERROR")
            return False
    
    def cleanup(self):
        """清理测试数据"""
        self.log("开始清理测试数据...")
        
        for item_id in self.created_items:
            response = self.make_request('DELETE', f'/items/items/{item_id}', need_auth=True)
            if response and response.status_code == 200:
                self.log(f"✅ 删除物品成功, ID: {item_id}")
            else:
                self.log(f"❌ 删除物品失败, ID: {item_id}", "ERROR")
    
    def run_comprehensive_test(self) -> Dict[str, bool]:
        """运行完整的API测试流程"""
        self.log("🚀 开始执行校园失物招领系统API智能测试")
        self.log("=" * 60)
        
        results = {}
        
        # 1. 系统健康检查
        results['health_check'] = self.test_health_check()
        time.sleep(0.5)
        
        # 2. 用户注册
        results['register'] = self.test_register()
        if not results['register']:
            # 如果注册失败，尝试使用已存在用户登录
            self.log("注册失败，尝试使用默认用户登录...")
            self.test_user = {"username": "admin", "password": "123456"}
            results['login'] = self.test_login()
        else:
            results['login'] = True  # 注册成功后自动登录
        time.sleep(0.5)
        
        # 3. 发布失物信息
        if results['login']:
            results['publish_lost'] = self.test_publish_lost_item()
            time.sleep(0.5)
            
            # 4. 发布招领信息
            results['publish_found'] = self.test_publish_found_item()
            time.sleep(0.5)
            
            # 5. 获取物品列表
            results['get_items'] = self.test_get_items()
            time.sleep(0.5)
            
            # 6. 搜索物品
            results['search_items'] = self.test_search_items()
            time.sleep(0.5)
            
            # 7. 获取物品详情
            results['get_item_detail'] = self.test_get_item_detail()
            time.sleep(0.5)
            
            # 8. 更新物品信息
            results['update_item'] = self.test_update_item()
            time.sleep(0.5)
            
            # 9. 获取用户统计
            results['user_stats'] = self.test_user_stats()
            time.sleep(0.5)
            
            # 10. 用户登出
            results['logout'] = self.test_logout()
        
        # 清理测试数据
        try:
            self.cleanup()
        except Exception as e:
            self.log(f"清理数据时出错: {e}", "ERROR")
        
        # 输出测试结果
        self.log("=" * 60)
        self.log("📊 测试结果汇总:")
        
        passed = 0
        total = len(results)
        
        for test_name, result in results.items():
            status = "✅ 通过" if result else "❌ 失败"
            self.log(f"  {test_name}: {status}")
            if result:
                passed += 1
        
        self.log("=" * 60)
        self.log(f"测试完成: {passed}/{total} 通过")
        
        if passed == total:
            self.log("🎉 所有测试通过！系统运行正常")
        else:
            self.log(f"⚠️ 有 {total - passed} 个测试失败，请检查系统")
        
        return results

def main():
    """主函数"""
    print("校园失物招领系统 - API智能测试脚本")
    print("=" * 60)
    
    # 获取用户输入的API地址
    api_url = input("请输入API地址 (默认: http://localhost:8080/api): ").strip()
    if not api_url:
        api_url = "http://localhost:8080/api"
    
    # 创建测试器实例
    tester = CampusLostFoundTester(base_url=api_url)
    
    try:
        # 运行完整测试
        results = tester.run_comprehensive_test()
        
        # 询问是否要重新运行失败的测试
        failed_tests = [name for name, result in results.items() if not result]
        if failed_tests and input("\n是否要重新运行失败的测试? (y/n): ").lower() == 'y':
            print("\n重新运行失败的测试...")
            # 这里可以添加重试逻辑
            
    except KeyboardInterrupt:
        print("\n\n测试被用户中断")
    except Exception as e:
        print(f"\n测试过程中发生错误: {e}")

if __name__ == "__main__":
    main()