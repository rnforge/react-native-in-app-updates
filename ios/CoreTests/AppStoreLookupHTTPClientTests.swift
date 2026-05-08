import Foundation
import XCTest
@testable import AppStoreLookupCore

private class MockURLProtocol: URLProtocol {
    static var requestHandler: ((URLRequest) throws -> (HTTPURLResponse, Data))?

    override class func canInit(with request: URLRequest) -> Bool {
        return true
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        return request
    }

    override func startLoading() {
        guard let handler = MockURLProtocol.requestHandler else {
            XCTFail("No request handler set")
            return
        }
        do {
            let (response, data) = try handler(request)
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: data)
            client?.urlProtocolDidFinishLoading(self)
        } catch {
            client?.urlProtocol(self, didFailWithError: error)
        }
    }

    override func stopLoading() {}
}

final class AppStoreLookupHTTPClientTests: XCTestCase {
    private var session: URLSession!

    override func setUp() {
        super.setUp()
        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [MockURLProtocol.self]
        session = URLSession(configuration: configuration)
    }

    override func tearDown() {
        session = nil
        MockURLProtocol.requestHandler = nil
        super.tearDown()
    }

    func testSuccessResponse() {
        let expectation = self.expectation(description: "success")
        let testData = Data("{\"resultCount\":1}".utf8)

        MockURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: 200,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, testData)
        }

        let client = AppStoreLookupHTTPClient(session: session)
        let url = URL(string: "https://itunes.apple.com/lookup?id=123")!

        client.performLookup(url: url) { result in
            switch result {
            case .success(let data):
                XCTAssertEqual(data, testData)
            case .failure:
                XCTFail("Expected success")
            }
            expectation.fulfill()
        }

        waitForExpectations(timeout: 1)
    }

    func testHTTPErrorResponse() {
        let expectation = self.expectation(description: "http error")

        MockURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(
                url: request.url!,
                statusCode: 404,
                httpVersion: nil,
                headerFields: nil
            )!
            return (response, Data())
        }

        let client = AppStoreLookupHTTPClient(session: session)
        let url = URL(string: "https://itunes.apple.com/lookup?id=123")!

        client.performLookup(url: url) { result in
            switch result {
            case .success:
                XCTFail("Expected failure")
            case .failure(let error):
                XCTAssertEqual(error, .httpError(statusCode: 404))
            }
            expectation.fulfill()
        }

        waitForExpectations(timeout: 1)
    }

    func testNetworkErrorResponse() {
        let expectation = self.expectation(description: "network error")

        MockURLProtocol.requestHandler = { _ in
            throw NSError(domain: NSURLErrorDomain, code: NSURLErrorNotConnectedToInternet)
        }

        let client = AppStoreLookupHTTPClient(session: session)
        let url = URL(string: "https://itunes.apple.com/lookup?id=123")!

        client.performLookup(url: url) { result in
            switch result {
            case .success:
                XCTFail("Expected failure")
            case .failure(let error):
                XCTAssertEqual(error, .networkError)
            }
            expectation.fulfill()
        }

        waitForExpectations(timeout: 1)
    }

    func testTimeoutErrorResponse() {
        let expectation = self.expectation(description: "timeout error")

        MockURLProtocol.requestHandler = { _ in
            throw NSError(domain: NSURLErrorDomain, code: NSURLErrorTimedOut)
        }

        let client = AppStoreLookupHTTPClient(session: session)
        let url = URL(string: "https://itunes.apple.com/lookup?id=123")!

        client.performLookup(url: url) { result in
            switch result {
            case .success:
                XCTFail("Expected failure")
            case .failure(let error):
                XCTAssertEqual(error, .timeout)
            }
            expectation.fulfill()
        }

        waitForExpectations(timeout: 1)
    }
}
