//
//  InterfaceController.swift
//  Test12 WatchKit Extension
//
//  Created by Terence Tsang on 2016-11-19.
//  Copyright © 2016 Terence Tsang. All rights reserved.
//

import WatchKit
import Foundation
import Alamofire
import WatchConnectivity

class InterfaceController: WKInterfaceController, WCSessionDelegate {
    var duration = 1.2
    var percentage = 0

    @IBOutlet var group: WKInterfaceGroup!
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        // Configure interface objects here.
        WCSession.default().delegate = self
        WCSession.default().activate()
        
    }
    
    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String : Any]) {
        percentage = (session.receivedApplicationContext as! [String: Int])["percentage"]!
    }
    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
    }

    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
        group.setBackgroundImageNamed("single" + percentage.description)
        group.startAnimatingWithImages(in: NSMakeRange(0, 97), duration: duration, repeatCount: 1)
    }
    
    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }
}
