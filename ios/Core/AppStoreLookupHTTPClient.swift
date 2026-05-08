import Foundation

enum LookupHTTPError: Error, Equatable {
    case timeout
    case networkError
    case noData
    case httpError(statusCode: Int)
}

struct AppStoreLookupHTTPClient {
    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    func performLookup(
        url: URL,
        timeout: TimeInterval = 5,
        completion: @escaping (Result<Data, LookupHTTPError>) -> Void
    ) {
        var request = URLRequest(url: url)
        request.timeoutInterval = timeout

        let task = session.dataTask(with: request) { data, response, error in
            if let error = error as NSError? {
                if error.code == NSURLErrorTimedOut {
                    completion(.failure(.timeout))
                } else {
                    completion(.failure(.networkError))
                }
                return
            }

            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                let statusCode = (response as? HTTPURLResponse)?.statusCode ?? 0
                completion(.failure(.httpError(statusCode: statusCode)))
                return
            }

            guard let data = data else {
                completion(.failure(.noData))
                return
            }

            completion(.success(data))
        }
        task.resume()
    }
}
