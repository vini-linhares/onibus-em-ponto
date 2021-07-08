//
//  TutorialViewCOntroller.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 27/09/17.
//   
//

import Foundation
import UIKit
import AVFoundation

class TutorialViewController: UIViewController, AVAudioPlayerDelegate{
    
    var player = AVAudioPlayer()
    var ctrl: Int = 0
    var isVoice: Bool = true
    var decideVoice: Bool = false
    
    var imagem: UIImage!
    
    
    @IBOutlet weak var btnDireita: UIButton!
    @IBOutlet weak var btnEsquerda: UIButton!
    @IBOutlet weak var imgFundo: UIButton!
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        
        let screenSize: CGRect = UIScreen.main.bounds
        btnDireita.frame = CGRect(x: screenSize.width * 0.5, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
        btnDireita.imageEdgeInsets = UIEdgeInsetsMake(btnDireita.frame.size.height, btnDireita.frame.size.width, btnDireita.frame.size.height, btnDireita.frame.size.width)
        
        btnEsquerda.frame = CGRect(x: 0, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
        btnEsquerda.imageEdgeInsets = UIEdgeInsetsMake(btnEsquerda.frame.size.height, btnEsquerda.frame.size.width, btnEsquerda.frame.size.height, btnEsquerda.frame.size.width)
        
        imgFundo.frame = CGRect(x: 0, y: screenSize.height * 0.05, width: screenSize.width, height: screenSize.height * 0.95)
        imgFundo.imageEdgeInsets = UIEdgeInsetsMake(imgFundo.frame.size.height, imgFundo.frame.size.width, imgFundo.frame.size.height, imgFundo.frame.size.width)
        
        if let path = Bundle.main.path(forResource: "a", ofType: "mp3"){
            let url = URL(fileURLWithPath: path)
            
            do{
                player = try AVAudioPlayer(contentsOf: url)
                player.delegate = self
                player.prepareToPlay()
                player.play()
            }catch{
                print("Erro ao execultar um som")
            }
        }
    }
    
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        
        self.ctrl += 1
        
        var path: String = ""
        
        switch self.ctrl {
        case 1:
            path = Bundle.main.path(forResource: "b", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "b")
        case 2:
            btnEsquerda.isHidden = false;
            btnDireita.isHidden = false;
            decideVoice = true;
        case 3:
            path = Bundle.main.path(forResource: "d", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "d")
        case 4:
            path = Bundle.main.path(forResource: "e", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "e")
        case 5:
            path = Bundle.main.path(forResource: "f", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "f")
        case 6:
            path = Bundle.main.path(forResource: "g", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "g")
        case 7:
            path = Bundle.main.path(forResource: "h", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "h")
        case 8:
            path = Bundle.main.path(forResource: "i", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "i")
        case 9:
            path = Bundle.main.path(forResource: "j", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "j")
        case 10:
            path = Bundle.main.path(forResource: "k", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "k")
        case 11:
            path = Bundle.main.path(forResource: "l", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "l")
        case 12:
            path = Bundle.main.path(forResource: "m", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "m")
        case 13:
            path = Bundle.main.path(forResource: "n", ofType: "mp3")!
            self.imagem = #imageLiteral(resourceName: "n")
        case 14:
            //self.dismiss(animated: true, completion: nil)
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        default:
            print("Some other character")
        }
        imgFundo.setImage(self.imagem, for: .normal)
        
        if !decideVoice{
            if path != nil{
                let url = URL(fileURLWithPath: path)
                do{
                    self.player = try AVAudioPlayer(contentsOf: url)
                    self.player.delegate = self
                    self.player.prepareToPlay()
                    self.player.play()
                }catch{
                    print("Erro ao execultar um som")
                }
            }
        }
    }
    
    @IBAction func btnDireita(_ sender: Any) {
        if decideVoice {
            isVoice = true;
            UserDefaults.standard.set("s", forKey: "salvaVoice")
            btnDireita.isHidden = true
            decideVoice = false
            if let path = Bundle.main.path(forResource: "c", ofType: "mp3"){
                let url = URL(fileURLWithPath: path)
                do{
                    self.player = try AVAudioPlayer(contentsOf: url)
                    self.player.delegate = self
                    self.player.prepareToPlay()
                    self.player.play()
                }catch{
                    print("Erro ao execultar um som")
                }
            }
            self.imagem = #imageLiteral(resourceName: "c")
            imgFundo.setImage(self.imagem, for: .normal)
        }
    }
    @IBAction func btnEsquerda(_ sender: Any) {
        if decideVoice {
            isVoice = false;
            btnDireita.isHidden = true
            btnEsquerda.isHidden = true
            decideVoice = false
            self.imagem = #imageLiteral(resourceName: "c")
            imgFundo.setImage(self.imagem, for: .normal)
        }else{
            //self.dismiss(animated: true, completion: nil)
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }
    }
    
    @IBAction func btnFundo(_ sender: Any) {
        if(!isVoice){
            self.ctrl += 1
            
            switch self.ctrl {
            case 1:
                self.imagem = #imageLiteral(resourceName: "b")
            case 2:
                self.imagem = #imageLiteral(resourceName: "c")
            case 3:
                self.imagem  = #imageLiteral(resourceName: "d")
            case 4:
                self.imagem = #imageLiteral(resourceName: "e")
            case 5:
                self.imagem = #imageLiteral(resourceName: "f")
            case 6:
                self.imagem = #imageLiteral(resourceName: "g")
            case 7:
                self.imagem = #imageLiteral(resourceName: "h")
            case 8:
                self.imagem = #imageLiteral(resourceName: "i")
            case 9:
                self.imagem = #imageLiteral(resourceName: "j")
            case 10:
                self.imagem = #imageLiteral(resourceName: "k")
            case 11:
                self.imagem = #imageLiteral(resourceName: "l")
            case 12:
                self.imagem = #imageLiteral(resourceName: "m")
            case 13:
                self.imagem = #imageLiteral(resourceName: "n")
            case 14:
                //self.dismiss(animated: true, completion: nil)
                if let navigationController = self.navigationController
                {
                    navigationController.popViewController(animated: true)
                }
            default:
                print("Some other character")
            }
            imgFundo.setImage(self.imagem, for: .normal)
        }
    }
}
