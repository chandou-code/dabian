import requests

# 接口地址
url = "http://localhost:18080/api/test/clear-lost-items"

try:
    # 发送DELETE请求
    response = requests.post(url)

    # 打印响应状态码
    print(f"状态码: {response.status_code}")

    # 打印响应内容
    if response.text:
        print(f"响应内容: {response.text}")
    else:
        print("请求成功，但响应体为空")

except requests.exceptions.ConnectionError:
    print("连接失败，请检查服务器是否运行在 localhost:18080")
except requests.exceptions.Timeout:
    print("请求超时")
except requests.exceptions.RequestException as e:
    print(f"请求发生错误: {e}")
