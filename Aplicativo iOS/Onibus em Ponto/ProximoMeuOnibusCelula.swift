//
//  ProximoMeuOnibusCelula.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit

class ProximoMeuOnibusCelula: UITableViewCell {
    
    
    @IBOutlet weak var imgMinibus: UIImageView!
    @IBOutlet weak var lblDist: UILabel!
    @IBOutlet weak var lblEnd: UILabel!
    
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}


