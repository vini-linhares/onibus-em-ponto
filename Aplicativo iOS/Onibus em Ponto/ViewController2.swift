//
//  ViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 27/09/17.
//   
//

import UIKit

class ViewController2: UITableViewController {
    
    var menus: [Menu] = []
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        var menu: Menu
        menu = Menu(imagem: #imageLiteral(resourceName: "btn1"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn2"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn3"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn4"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn5"))
        menus.append(menu)
        
        //TutorialViewController
        let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "TutorialViewController") as! TutorialViewController
        self.navigationController!.pushViewController(VC1, animated: true)
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return menus.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let menu: Menu = menus[indexPath.row]
        let celulaReuso = "homeCelulaReuso"
        let celula = tableView.dequeueReusableCell(withIdentifier: celulaReuso, for: indexPath) as! HomeCelula
        
        //celula.imageView?.image = menu.imagem
        
        celula.homeImageView.image = menu.imagem
        
        return celula
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        if indexPath.row == 3{
            //-self.performSegue(withIdentifier: "OndeEstouPassandoViewController", sender:self)
            //--let viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "OndeEstouPassandoViewController")
            //--self.present(viewController, animated: false, completion: nil)
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "OndeEstouPassandoViewController") as! MeuLocalViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
        if indexPath.row == 0{
            //self.performSegue(withIdentifier: "OndeEstouPassandoViewController", sender:self)
            //let viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "TutorialViewController")
            //self.present(viewController, animated: false, completion: nil)
            
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "testeV") as! ViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
}

