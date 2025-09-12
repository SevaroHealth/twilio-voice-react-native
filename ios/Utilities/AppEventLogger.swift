//
//  Untitled.swift
//  Triage
//
//  Created by Aklesh rathaur on 26/08/25.
//
import Foundation

@objc public enum LogType: Int {
    case info = 0
    case error = 1
    case warn = 2

    public var description: String {
        switch self {
        case .info: return "info"
        case .error: return "error"
        case .warn: return "warn"
        }
    }
}

@objc public class AppEventLogger: NSObject {
  @objc nonisolated(unsafe) public static let shared = AppEventLogger()
    private override init() {}

    @objc public func log(_ message: String, type: LogType) {
        callLogger(message, type: type.description)
    }

    // Overload for ObjC that doesn’t pass type (defaults to .info)
    @objc public func log(_ message: String) {
        callLogger(message, type: LogType.info.description)
    }

    @objc public func logError(_ message: String) {
        callLogger(message, type: LogType.error.description)
    }

    private func callLogger(_ data: String, type: String) {
     // for Dev/QA:- https://synapse-dev.cloud.mysevaro.com/addLog
    // for Prod:- https://synapse.cloud.mysevaro.com/

        let baseURL = "https://synapse.cloud.mysevaro.com/"
        let fullURLString = baseURL + "addLog"

        guard let url = URL(string: fullURLString) else {
            print("[Log] Invalid URL: \(fullURLString)")
            return
        }
        
        var userIDString = ""
        var userNameString = ""

        
        if let userName = UserDefaults.standard.value(forKey: "userName") as? String, let userID = UserDefaults.standard.value(forKey: "userID") as? String {
           userNameString = userName
           userIDString = userID
            print("userName is \(userNameString), userID is \(userIDString)")
        }
        
        var appVersionString = ""

        if let appVersion = UserDefaults.standard.value(forKey:"appVersionString" ) as? String{
            appVersionString = appVersion
            print("appVersionString is \(appVersionString)")
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let bodyString = "data=\(data)ANDlogType=\(type)ANDuserName=\(userNameString)ANDuserID=\(userIDString)ANDappVersion=\(appVersionString)"
        request.httpBody = bodyString.data(using: .utf8)
        request.setValue("synapse", forHTTPHeaderField: "User-Agent")

        print("Sending log \(data) to: \(fullURLString)")

        let task = URLSession.shared.dataTask(with: request) { _, response, error in
            if let error = error {
                print("[Log] Error sending log:", error)
                return
            }

            if let httpResponse = response as? HTTPURLResponse {
                print("[Log] Response status code:", httpResponse.statusCode)
            }
        }
        task.resume()
    }
}
