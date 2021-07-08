//
//  SelecionarOnibusVIewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 03/10/17.
//   
//

import UIKit
import MapKit
import AVFoundation // Audio
import Speech       //Ouvir

class SelecionarOnibusVIewController: UIViewController, UITableViewDataSource, UITableViewDelegate, CLLocationManagerDelegate, AVAudioPlayerDelegate {
    
    
    var speechTimer = Timer()
    @objc public func updateCounterSpeech() {
        speechVerificar()
    }
    func speechVerificar(){
        if self.comando == self.comandoAntigo{
            print("Igual - Parou")
            pararOuvir()
            speechTimer.invalidate()
        }else{
            self.comandoAntigo = self.comando
            print("DIFERENTE - CONTINUA")
        }
    }
    
    
    var gerenciadorLocalizacao = CLLocationManager()
    
    var data: [SelecionarOnibusDadosTabela] = []
    
    
    @IBOutlet weak var btnPesquisar: UIButton!
    @IBOutlet weak var lblEnd: UILabel!
    @IBOutlet weak var txtLinha: UITextField!
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    
    @IBOutlet weak var btnSelecionar: UIButton!
    
    var escrevendo = false
    var pesquisado = false
    
    var selecionados = 0
    
    var coordenadas: String = ""
    
    var nenhumOnibus = false
    
    var latitude = "0"
    var longitude = "0"
    
    //Falar
    let synth = AVSpeechSynthesizer()
    var myUtterance = AVSpeechUtterance(string: "")
    //var falando = false
    var ruaAnterior = ""
    var numAnterior = ""
    var isVoice = false
    var salvaVoice = "salvaVoice"
    var destinoIdentificado = false
    var mensagemInicio = true
    var falarDistancia = false
    var linhaPorFala = -1
    
    //Ouvir
    enum SpeechStatus {
        case ready
        case recognizing
        case unavailable
    }
    var status = SpeechStatus.ready
    let audioEngine = AVAudioEngine()
    let speechRecognizer: SFSpeechRecognizer? = SFSpeechRecognizer(locale: Locale(identifier: "pt-BR"))
    let request = SFSpeechAudioBufferRecognitionRequest()
    var recognitionTask: SFSpeechRecognitionTask?
    var comando = ""
    var comandoAntigo = ""
    var player = AVAudioPlayer()
    
    var idPonto = ""
    var endPonto = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setVoltando(voz: false)
        
        gerenciadorLocalizacao.delegate = self
        gerenciadorLocalizacao.desiredAccuracy = kCLLocationAccuracyBest
        gerenciadorLocalizacao.requestWhenInUseAuthorization()
        gerenciadorLocalizacao.startUpdatingLocation()
        
        UITableView.dataSource = self
        UITableView.delegate = self
        
        if getVoice() == "s"{
            //Botão
            let screenSize: CGRect = UIScreen.main.bounds
            btnDireita.frame = CGRect(x: screenSize.width * 0.5, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
            btnDireita.imageEdgeInsets = UIEdgeInsetsMake(btnDireita.frame.size.height, btnDireita.frame.size.width, btnDireita.frame.size.height, btnDireita.frame.size.width)
            print(String(describing: screenSize.width) + "-" + String(describing: screenSize.width))
            //Ouvir
            switch SFSpeechRecognizer.authorizationStatus() {
            case .notDetermined:
                askSpeechPermission()
            case .authorized:
                self.status = .ready
            case .denied, .restricted:
                self.status = .unavailable
            }
            isVoice = true
        }else{
            btnDireita.isHidden = true
            isVoice = false
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        synth.stopSpeaking(at: AVSpeechBoundary.immediate)
        super.viewDidDisappear(animated)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if isVoice && getVoltando() == "s"{
            setVoltando(voz: false)
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }
    }
    
    func getVoltando() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: "Voltando"){
            return voiceSalva as! String
        }
        return "n"
    }
    func setVoltando(voz: Bool){
        if(voz){
            UserDefaults.standard.set("s", forKey: "Voltando")
        }else{
            UserDefaults.standard.set("n", forKey: "Voltando")
        }
    }
    
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let localizacaoUsuario = locations.last!
        
        self.latitude = String(localizacaoUsuario.coordinate.latitude)
        self.longitude = String(localizacaoUsuario.coordinate.longitude)
        
        if !pesquisado {
            //if !self.isVoice{
                buscarPontos(latlng: self.latitude + "," + self.longitude)
            //}
            pesquisado = true
            /*
            CLGeocoder().reverseGeocodeLocation(localizacaoUsuario) { (detalhesLocal, erro) in
                if erro == nil{
                    if let dadosLocal = detalhesLocal?.first{
                        var rua = ""
                        if dadosLocal.thoroughfare != nil{
                            rua = dadosLocal.thoroughfare!
                        }
                        var num = ""
                        if dadosLocal.subThoroughfare != nil{
                            num = dadosLocal.subThoroughfare!
                        }
                        
                        self.lblEnd.text = rua + ", " + num
                        
                        if self.isVoice{
                            
                            if self.mensagemInicio{
                                self.falar(texto: "Buscando linhas que passam em " + rua + ", número " + num + ". Se estiver correto diga: Buscar.")
                                self.mensagemInicio = false
                            }
                        }
                        
                    }
                }else{
                    print("Erro")
                }
            }
            */
        }
    }
    
    func buscarLinhas(ponto: String){
        self.montarTabelaErro(valor: 2)
        //var latlng2 = "-23.484029,-46.584321"
        //self.coordenadas = latlng
        if let url = URL(string: "http://onibusemponto.com/api2/pegaLinhasPonto/?ponto="+ponto){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                //var backToString = String(data: dados!, encoding: String.Encoding.utf8) as String!
                //var somedata = backToString?.data(using: String.Encoding.utf8)
                print("http://onibusemponto.com/api2/pegaLinhasPonto/?ponto="+ponto)
                if erro == nil{
                    if let dadosRetorno = dados{
                        //print(dadosRetorno)
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                //print(objetoJson)
                                if let linhas = objetoJson["linhas"]{
                                    //print(linhas)
                                    if let array = linhas as? [Any]{
                                        if let um = array.first{
                                            //print(um)
                                            
                                            if let objAtual = um as? [String: Any]{
                                                if let numero = objAtual["numero"] as? String{
                                                    if numero == "0000"{
                                                        self.nenhumOnibus = true
                                                        self.montarTabelaErro(valor: 0)
                                                    }else{
                                                        self.nenhumOnibus = false
                                                        self.montarTabela(dados: array)
                                                        if self.isVoice{
                                                            DispatchQueue.main.async {
                                                                self.pegarTodos()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
                    }
                }else{
                    print("Erro")
                }
            }
            tarefa.resume()
        }
    }
    
    func buscarPontos(latlng: String){
        //self.montarTabelaErro(valor: 2)
        //var latlng2 = "-23.484029,-46.584321"
        self.coordenadas = latlng
        let lat = latlng.split(separator: ",")[0]
        let lng = latlng.split(separator: ",")[1]
        if let url = URL(string: "http://onibusemponto.com/api2/pegaPontos/?lat="+lat+"&lng="+lng){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                //var backToString = String(data: dados!, encoding: String.Encoding.utf8) as String!
                //var somedata = backToString?.data(using: String.Encoding.utf8)
                print("http://onibusemponto.com/api2/pegaPontos/?lat="+self.coordenadas)
                if erro == nil{
                    if let dadosRetorno = dados{
                        //print(dadosRetorno)
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                //print(objetoJson)
                                if let pontos = objetoJson["pontos"]{
                                    //print(linhas)
                                    if let array = pontos as? [Any]{
                                        if let um = array.first{
                                            //print(um)
                                            
                                            if let objAtual = um as? [String: Any]{
                                                if let id = objAtual["id"] as? String{
                                                    if id == "-1"{
                                                        DispatchQueue.main.async {
                                                            self.lblEnd.text = "Nenhum ponto nas proximidades"
                                                        }
                                                    }else{
                                                        DispatchQueue.main.async {
                                                            self.idPonto = objAtual["id"] as! String
                                                            self.endPonto = (objAtual["rua"] as? String)! + ", " + (objAtual["num"] as? String)!
                                                            self.lblEnd.text = (objAtual["rua"] as? String)! + ", " + (objAtual["num"] as? String)!
                                                            if self.isVoice{
                                                                self.falar(texto: "O ponto mais próximo está em " + (objAtual["rua"] as? String)! + ", número " + (objAtual["num"] as? String)! + ". Se estiver correto diga: Buscar. Se não diga: Buscar em, mais o local desejado.")
                                                                    self.mensagemInicio = false
                                                            }else{
                                                                self.buscarLinhas(ponto: objAtual["id"] as! String)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
                    }
                }else{
                    print("Erro")
                }
            }
            tarefa.resume()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status != .authorizedWhenInUse{
            let alertaController = UIAlertController(title: "Permissão de localização",
                                                     message: "Necessário permissão para acesso à sua localização",
                                                     preferredStyle: .alert)
            let acaoConfiguracoes = UIAlertAction(title: "Abrir Configurações", style: .default, handler: { (alertaConfiguracoes) in
                if let configuracoes = NSURL(string: UIApplicationOpenSettingsURLString){
                    UIApplication.shared.openURL(configuracoes as URL)
                }
            })
            let acaoCancelar = UIAlertAction(title: "Cancelar", style: .default, handler: nil)
            alertaController.addAction(acaoConfiguracoes)
            alertaController.addAction(acaoCancelar)
            present(alertaController, animated: true, completion: nil)
        }
    }
    
    func montarTabelaErro(valor: Int){
        data.removeAll()
        var dado: SelecionarOnibusDadosTabela
        var texto = ""
        if valor == 0{
            texto = "Nenhuma linha identificada"
        }else if valor == 1{
            texto = "SPtrans fora do Ar"
        }else {
            texto = "Buscando Linhas"
        }
        dado = SelecionarOnibusDadosTabela(numero: "", nome: texto, operacao: 0, sentido: 0)
        if self.isVoice{
            self.falar(texto: texto)
        }
        data.append(dado)
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! SelecionarOnibusCelula
            cell.imgMinilinha.isHidden = true
            cell.lblLinha.isHidden = true
        }
    }
    
    func montarTabela(dados: Array<Any>){
        print(dados[0])
        data.removeAll()
        
        var dado: SelecionarOnibusDadosTabela
        for i in 0...dados.count-1 {
            let objAtual = dados[i] as? [String: Any]
            dado = SelecionarOnibusDadosTabela(numero: objAtual!["numero"] as! String, nome: objAtual!["nome"] as! String, operacao: objAtual!["operacao"] as! Int, sentido: objAtual!["sentido"] as! Int)
            
            data.append(dado)
        }
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! SelecionarOnibusCelula
            cell.imgMinilinha.isHidden = false
            cell.lblLinha.isHidden = false
            self.UITableView.reloadData()
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "SelecionarOnibusCelula") as! SelecionarOnibusCelula
        let obj = data[indexPath.row]
        
        cell.lblLinha?.text = obj.numero + "-" + String(obj.operacao)
        cell.lblNome?.text = obj.nome
        cell.lblFundo?.isHidden = !obj.selecionado
        
        return cell //4.
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath as IndexPath, animated: true)
        let cell = tableView.cellForRow(at: indexPath)  as! SelecionarOnibusCelula
        
        if self.data[indexPath.row].selecionado {
            self.data[indexPath.row].selecionado = false
            cell.lblFundo.isHidden = true
            self.selecionados -= 1
        }else{
            self.data[indexPath.row].selecionado = true
            cell.lblFundo.isHidden = false
            self.selecionados += 1
        }
        if self.selecionados > 0{
            btnSelecionar.isEnabled = true
        }else{
            btnSelecionar.isEnabled = false
        }
    }
    
    
    
    
    func buscarEnd(end: String){
        //print("Entrou")
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/PegaLatlng/?end="+subs(texto: end)){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                //var backToString = String(data: dados!, encoding: String.Encoding.utf8) as String!
                //var somedata = backToString?.data(using: String.Encoding.utf8)
                //print(backToString)
                if erro == nil{
                    if let dadosRetorno = dados{
                        //print(dadosRetorno)
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                print(objetoJson)
                                if let lat = objetoJson["lat"] as? Double{
                                    if lat != 0{
                                        DispatchQueue.main.async {
                                            self.lblEnd.text = objetoJson["rua"] as! String + ", " + (objetoJson["num"] as! String)
                                        }
                                        self.buscarPontos(latlng: String(objetoJson["lat"] as! Double) + "," + String(objetoJson["lng"] as! Double))
                                    }else{
                                        DispatchQueue.main.async {
                                            self.lblEnd.text = "Endereço não Encontrado"
                                            self.falar(texto: "Endereço não Encontrado")
                                        }
                                    }
                                    
                                    //if let array = linhas as? [Any]{
                                    //    if let um = array.first{
                                    //        //print(um)
                                    //        self.montarTabela(dados: array)
                                    //    }
                                    //}
                                }
                            }
                        }catch{
                            print("Erro no Retorno")
                        }
                    }else{
                        print("erro nos dados")
                    }
                    
                    
                }else{
                    print("Erro")
                }
            }
            tarefa.resume()
        }else{
            print("erro na url")
        }
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ProximoSelecionarOnibus"{
            var linhas: [ProximoSelecionarOnibusModelo] = []
            for i in 0...data.count-1 {
                if data[i].selecionado{
                    let linha = ProximoSelecionarOnibusModelo(linha: data[i].numero, operacao: String(data[i].operacao), sentido: String(data[i].sentido), nome: data[i].nome, latlng: self.coordenadas)
                    linhas.append(linha)
                }
            }
            let vcDestino = segue.destination as! ProximoSelevionarOnibusViewController
            vcDestino.linhas = linhas
        }
    }
    
    /*
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ProximoSelecionarOnibus"{
            var linhas: [ProximoSelecionarOnibusModelo] = []
            for i in 0...data.count-1 {
                if data[i].selecionado{
                    let linha = ProximoSelecionarOnibusModelo(linha: data[i].numero, operacao: String(data[i].operacao), sentido: String(data[i].sentido), nome: data[i].nome, latlng: self.coordenadas)
                    linhas.append(linha)
                }
            }
            let vcDestino = segue.destination as! ProximoSelevionarOnibusViewController
            vcDestino.linhas = linhas
        }
    }
 */
    
    @IBAction func btnChamar(_ sender: Any) {
        var linhas: [ProximoSelecionarOnibusModelo] = []
        for i in 0...data.count-1 {
            if data[i].selecionado{
                let linha = ProximoSelecionarOnibusModelo(linha: data[i].numero, operacao: String(data[i].operacao), sentido: String(data[i].sentido), nome: data[i].nome, latlng: self.coordenadas)
                linhas.append(linha)
            }
        }
        if linhas.count > 1{
            let vcDestino = self.storyboard!.instantiateViewController(withIdentifier: "ProximoSelecionarOnibus") as! ProximoSelevionarOnibusViewController
            vcDestino.linhas = linhas
            vcDestino.idPonto = self.idPonto
            vcDestino.endPonto = self.endPonto
            self.navigationController!.pushViewController(vcDestino, animated: true)
        }else{
            let vcDestino = self.storyboard!.instantiateViewController(withIdentifier: "ProximoMeuOnibus") as! ProximoMeuOnibusViewController
            vcDestino.linha = linhas[0].linha
            vcDestino.operacao = linhas[0].operacao
            vcDestino.sentido = linhas[0].sentido
            vcDestino.latlng = self.coordenadas
            vcDestino.idPonto = self.idPonto
            vcDestino.endPonto = self.endPonto
            vcDestino.textLinha = linhas[0].linha + "-" + linhas[0].operacao + " " + linhas[0].nome
            
            self.navigationController!.pushViewController(vcDestino, animated: true)
        }
        
    }
    
    
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if self.nenhumOnibus  {
            return false
        }
        return true
    }
    
    func pegarTodos(){
        var linhas: [ProximoSelecionarOnibusModelo] = []
        for i in 0...data.count-1 {
                let linha = ProximoSelecionarOnibusModelo(linha: data[i].numero, operacao: String(data[i].operacao), sentido: String(data[i].sentido), nome: data[i].nome, latlng: self.coordenadas)
                linhas.append(linha)
        }
        if linhas.count > 1{
            let vcDestino = self.storyboard!.instantiateViewController(withIdentifier: "ProximoSelecionarOnibus") as! ProximoSelevionarOnibusViewController
            vcDestino.linhas = linhas
            vcDestino.idPonto = self.idPonto
            vcDestino.endPonto = self.endPonto
            self.navigationController!.pushViewController(vcDestino, animated: true)
        }else{
            let vcDestino = self.storyboard!.instantiateViewController(withIdentifier: "ProximoMeuOnibus") as! ProximoMeuOnibusViewController
            vcDestino.linha = linhas[0].linha
            vcDestino.operacao = linhas[0].operacao
            vcDestino.sentido = linhas[0].sentido
            vcDestino.latlng = self.coordenadas
            vcDestino.idPonto = self.idPonto
            vcDestino.endPonto = self.endPonto
            vcDestino.textLinha = linhas[0].linha + "-" + linhas[0].operacao + " " + linhas[0].nome
            
            self.navigationController!.pushViewController(vcDestino, animated: true)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func btnPesq(_ sender: Any) {
        
        self.txtLinha.resignFirstResponder()
        
        if escrevendo{
            let imagem: UIImage = #imageLiteral(resourceName: "miniponto")
            self.btnPesquisar.setImage(imagem, for: .normal)
            if let tamanho = txtLinha.text?.count{
                if tamanho >= 2{
                    self.lblEnd.text = "Buscando: " + txtLinha.text!
                    
                    //Acionar o Método de Pesquisar endereco, Depois buscar linhas
                    buscarEnd(end: txtLinha.text!)
                }
            }
            lblEnd.isHidden = false
            txtLinha.isHidden = true
            escrevendo = false
        }else{
            let imagem: UIImage = #imageLiteral(resourceName: "certo")
            self.btnPesquisar.setImage(imagem, for: .normal)
            txtLinha.text = ""
            lblEnd.isHidden = true
            txtLinha.isHidden = false
            escrevendo = true
        }
    }
    
    func subs(texto: String) -> String{
        let s1 = texto
        var s2 = trocaAcento(texto: s1)
        s2 = s2.replacingOccurrences(of: " ", with: "%20")
        return s2
    }
    
    func trocaAcento(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: "á", with: "a")
        s2 = s2.replacingOccurrences(of: "ã", with: "a")
        s2 = s2.replacingOccurrences(of: "à", with: "a")
        s2 = s2.replacingOccurrences(of: "â", with: "a")
        s2 = s2.replacingOccurrences(of: "é", with: "e")
        s2 = s2.replacingOccurrences(of: "ê", with: "e")
        s2 = s2.replacingOccurrences(of: "í", with: "i")
        s2 = s2.replacingOccurrences(of: "î", with: "i")
        s2 = s2.replacingOccurrences(of: "ó", with: "o")
        s2 = s2.replacingOccurrences(of: "ô", with: "o")
        s2 = s2.replacingOccurrences(of: "õ", with: "o")
        s2 = s2.replacingOccurrences(of: "ú", with: "u")
        s2 = s2.replacingOccurrences(of: "û", with: "u")
        s2 = s2.replacingOccurrences(of: "ç", with: "c")
        return s2
    }
    
    func corrigeFonemas(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: "el", with: "l")
        return s2
    }
    
    func trocaAbrev(texto: String) -> String{
        let s1 = texto
        var s2 = s1
        s2 = s2.replacingOccurrences(of: "VL.", with: "Vila");
        s2 = s2.replacingOccurrences(of: "JD.", with: "Jardim");
        s2 = s2.replacingOccurrences(of: "TERM.", with: "Terminal");
        s2 = s2.replacingOccurrences(of: "PRINC.", with: "Princesa");
        s2 = s2.replacingOccurrences(of: "PQ.", with: "Parque");
        s2 = s2.replacingOccurrences(of: "D.", with: "Dom");
        s2 = s2.replacingOccurrences(of: "CONJ.", with: "Conjunto");
        s2 = s2.replacingOccurrences(of: "HAB.", with: "Habitacional");
        s2 = s2.replacingOccurrences(of: "PÇA.", with: "Praça");
        s2 = s2.replacingOccurrences(of: "R.", with: "Rua");
        s2 = s2.replacingOccurrences(of: "AV.", with: "Avenida");
        s2 = s2.replacingOccurrences(of: "PTE.", with: "Ponte");
        s2 = s2.replacingOccurrences(of: "DR.", with: "Doutor");
        s2 = s2.replacingOccurrences(of: "BR.", with: "Barão");
        s2 = s2.replacingOccurrences(of: "HOSP.", with: "Hospital");
        s2 = s2.replacingOccurrences(of: "SHOP.", with: "Shopping");
        return s2;
    }
    func destrocaAbrev(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: "vila", with: "VL.");
        s2 = s2.replacingOccurrences(of: "jardim", with: "JD.");
        s2 = s2.replacingOccurrences(of: "terminal", with: "TERM.");
        s2 = s2.replacingOccurrences(of: "princesa", with: "PRINC.");
        s2 = s2.replacingOccurrences(of: "parque", with: "PQ.");
        s2 = s2.replacingOccurrences(of: "dom", with: "D.");
        s2 = s2.replacingOccurrences(of: "conjunto", with: "CONJ.");
        s2 = s2.replacingOccurrences(of: "habitacional", with: "HAB.");
        s2 = s2.replacingOccurrences(of: "praça", with: "PÇA.");
        s2 = s2.replacingOccurrences(of: "rua", with: "R.");
        s2 = s2.replacingOccurrences(of: "avenida", with: "AV.");
        s2 = s2.replacingOccurrences(of: "ponte", with: "PTE.");
        s2 = s2.replacingOccurrences(of: "doutor", with: "DR.");
        s2 = s2.replacingOccurrences(of: "barão", with: "BR.");
        s2 = s2.replacingOccurrences(of: "hospital", with: "HOSP.");
        s2 = s2.replacingOccurrences(of: "shopping", with: "SHOP.");
        return s2;
    }
    
    
    //Falar
    func falar(texto: String){
        myUtterance = AVSpeechUtterance(string: texto)
        myUtterance.rate = 0.45
        synth.speak(myUtterance)
    }
    
    //
    //Ouvir
    //
    func askSpeechPermission() {
        SFSpeechRecognizer.requestAuthorization { status in
            OperationQueue.main.addOperation {
                switch status {
                case .authorized:
                    self.status = .ready
                default:
                    self.status = .unavailable
                }
            }
        }
    }
    func startRecording() {
        self.comando = ""
        self.comandoAntigo = " "
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(SelecionarOnibusVIewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
        print("1")
        // Setup audio engine and speech recognizer
        let node = audioEngine.inputNode// else { return } //Tirei um guard da frente
        let recordingFormat = node.outputFormat(forBus: 0)
        node.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { buffer, _ in
            self.request.append(buffer)
        }
        print("2")
        // Prepare and start recording
        audioEngine.prepare()
        do {
            try audioEngine.start()
            self.status = .recognizing
        } catch {
            print("Errinho")
            return print(error)
        }
        print("3")
        // Analyze the speech
        recognitionTask = speechRecognizer?.recognitionTask(with: request, resultHandler: { result, error in
            if let result = result {
                print("4")
                print(result.bestTranscription.formattedString)
                self.comando = result.bestTranscription.formattedString
            } else if let error = error {
                print(error)
                print("Errinho 2")
            }
        })
    }
    func cancelRecording() {
        tocaSons(qual: 3)
        audioEngine.stop()
        let node = audioEngine.inputNode //{ //Tirei um if
        node.removeTap(onBus: 0)
        //}
        recognitionTask?.cancel()
        self.reconhecerComando()
    }
    func reconhecerComando(){
        let comand = self.comando.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
        print(comand)
        //print(comand.lowercased().range(of: "buscar em"))
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else if comand == "buscar" {
            //buscarPontos(latlng: self.latitude + "," + self.longitude)
            self.buscarLinhas(ponto: self.idPonto)
        }else if comand.lowercased().range(of: "buscar em") != nil{
            let coman = comand.replacingOccurrences(of: "buscar em", with: "")
            print("Buscar em - " + coman)
            if coman.count >= 0{
                self.buscarEnd(end: coman)
            }
        }/*else if comand.lowercased().range(of: "linha") != nil{
            var coman = comand.replacingOccurrences(of: "linha", with: "")
            print("linha - " + coman)
            if coman.count >= 0{
                self.buscarEmLinhas(linha: coman)
            }
        }*/
    }
/*
    func buscarEmLinhas(linha: String){
        var dado: SelecionarOnibusDadosTabela
        for i in 0...self.data.count-1 {
            dado = data[i]
            var linhaComparar = dado.numero + " " + String(dado.operacao) + " " + dado.nome
            linhaComparar = linhaComparar.replacingOccurrences(of: " ", with: "")
            linhaComparar = linhaComparar.lowercased()
            var linha2 = destrocaAbrev(texto: linha)
            linha2 = linha2.replacingOccurrences(of: " ", with: "")
            linha2 = linha2.lowercased()
            print(linhaComparar)
            print(linha2)
            if (!self.nenhumOnibus) && ((linha2 == linhaComparar) || (corrigeFonemas(texto: trocaAcento(texto: linha2)) == corrigeFonemas(texto: trocaAcento(texto: linhaComparar)))){
                self.linhaPorFala = i
                let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "ProximoSelecionarOnibus") as! ProximoSelevionarOnibusViewController
                
                let linha = self.data[linhaPorFala].numero
                let op = self.data[linhaPorFala].operacao
                let sentido = self.data[linhaPorFala].sentido
                VC1.linha = linha!
                if let ope = op?.description{
                    VC1.operacao = ope
                }
                if let sen = sentido?.description{
                    VC1.sentido = sen
                }
                VC1.latlng = self.coordenadas
                
                self.navigationController!.pushViewController(VC1, animated: true)
                break
            }
        }
    }
 */
    @IBAction func btnOuvirIniciar(_ sender: Any) {
        if status != .recognizing{
            print("Ouvindo")
            startRecording()
            status = .recognizing
            synth.stopSpeaking(at: AVSpeechBoundary.immediate) // Parar fala
            tocaSons(qual: 1)
        }
    }
    
    @IBAction func btnOuvirFinalizar(_ sender: Any) {
        //pararOuvir()
    }
    func pararOuvir(){
        print("Parando")
        status = .ready
        cancelRecording()
    }
    func tocaSons(qual: Int){
        //Inicio -> 1
        //Erro -> 2
        //Fim -> 3
        var arquivo: String = ""
        switch qual {
        case 1:
            arquivo = "inicio"
        case 2:
            arquivo = "erro"
        case 3:
            arquivo = "fim"
        default:
            arquivo = ""
        }
        if let path = Bundle.main.path(forResource: arquivo, ofType: "mp3"){
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
    
}
