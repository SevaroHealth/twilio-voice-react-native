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
    case other = 2

    public var description: String {
        switch self {
        case .info: return "Information"
        case .error: return "Error"
        case .other: return "Other"
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
        let baseURL = "https://synapse-dev.cloud.mysevaro.com/"
        let fullURLString = baseURL + "addLog"

        guard let url = URL(string: fullURLString) else {
            print("[Log] Invalid URL: \(fullURLString)")
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let bodyString = "data=\(data)&logType=\(type)"
        request.httpBody = bodyString.data(using: .utf8)

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
