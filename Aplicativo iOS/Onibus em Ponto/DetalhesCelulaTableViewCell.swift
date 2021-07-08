//
//  DetalhesCelulaTableViewCell.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 29/09/17.
//
//

import UIKit

class DetalhesCelulaTableViewCell: UITableViewCell {

    @IBOutlet weak var lblLinha: UILabel!
    @IBOutlet weak var lblIda: UILabel!
    @IBOutlet weak var lblVolta: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
