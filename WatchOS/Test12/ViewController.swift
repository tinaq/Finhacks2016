//
//  ViewController.swift
//  Test12
//
//  Created by Terence Tsang on 2016-11-19.
//  Copyright © 2016 Terence Tsang. All rights reserved.
//

import UIKit
import FirebaseDatabase
import WatchConnectivity

class ViewController: UIViewController, WCSessionDelegate {
    
    var session: WCSession?

    override func viewDidLoad() {
        super.viewDidLoad()
        if (WCSession.isSupported()) {
            session = WCSession.default()
            session?.delegate = self
            session?.activate()
        }
        // Do any additional setup after loading the view, typically from a nib.
        var ref: FIRDatabaseReference!
        ref = FIRDatabase.database().reference()
        ref.observe(FIRDataEventType.value, with: { (snapshot) in
            let postDict = snapshot.value as? [String : AnyObject] ?? [:]
            print(postDict)
            var contributed = postDict["goal"]?["contributed"] as! Double
            var balance = postDict["account"]?["balance"] as! Double
            
            var dict: [String: Any] = ["percentage": Int(ceil(((contributed/balance)*100.0))).description]
            
            if let validSession = self.session {
                
                do {
                    try validSession.updateApplicationContext(dict)
                } catch {
                    print("Something went wrong")
                }
            }
 
        })
        
    }
    
    func sessionDidBecomeInactive(_ session: WCSession) {
        
    }
    
    func sessionDidDeactivate(_ session: WCSession) {
        
    }
    
    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {
        print("Done Syncing")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

