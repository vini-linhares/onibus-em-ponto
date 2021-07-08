//
//  Meu Ônibus.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//

import UIKit
import MapKit
import AVFoundation // Audio
import Speech       //Ouvir

class MeuOnibus: UIViewController, UITableViewDataSource, CLLocationManagerDelegate, AVAudioPlayerDelegate{
    
    
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
    
    var data: [MeuOnibusDadosTabela] = []
    
    @IBOutlet weak var btnPesquisar: UIButton!
    @IBOutlet weak var lblEnd: UILabel!
    @IBOutlet weak var txtLinha: UITextField!
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    
    var escrevendo = false
    var pesquisado = false
    
    var coordenadas: String = ""
    
    var nenhumOnibus = false
    
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
    
    
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setVoltando(voz: false)
        
        gerenciadorLocalizacao.delegate = self
        gerenciadorLocalizacao.desiredAccuracy = kCLLocationAccuracyBest
        gerenciadorLocalizacao.requestWhenInUseAuthorization()
        gerenciadorLocalizacao.startUpdatingLocation()
        
        UITableView.dataSource = self
        
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
        
        let latitude = localizacaoUsuario.coordinate.latitude
        let longitude = localizacaoUsuario.coordinate.longitude
        if !pesquisado {
            buscarLinhas(latlng: String(latitude) + "," + String(longitude))
            pesquisado = true
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
                                self.falar(texto: "Buscando linhas em " + rua + ", número " + num + ".")
                                self.mensagemInicio = false
                            }
                        }
                        
                        
                    }
                }else{
                    print("erro")
                }
            }
        }
    }
    
    func buscarLinhas(latlng: String){
        //var latlng2 = "-23.484029,-46.584321"
        self.montarTabelaErro(valor: 2)
        self.coordenadas = latlng
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/pegalinhascompl?latlng="+self.coordenadas){
            print(url)
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                //var backToString = String(data: dados!, encoding: String.Encoding.utf8) as String!
                //var somedata = backToString?.data(using: String.Encoding.utf8)
                //print(backToString)
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
                    print("erro")
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
        var dado: MeuOnibusDadosTabela
        var texto = ""
        if valor == 0{
            texto = "Nenhuma linha identificada"
        }else if valor == 1{
            texto = "SPtrans fora do Ar"
        }else {
            texto = ""
        }
        dado = MeuOnibusDadosTabela(numero: "", nome: texto, operacao: 0, sentido: 0)
        if self.isVoice{
            self.falar(texto: texto)
        }
        data.append(dado)
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            self.UITableView.reloadData()
            
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! MeuOnibusCelula
            cell.imgMiniLinha.isHidden = true
            cell.txtLinha.isHidden = true
        }
    }
    
    func montarTabela(dados: Array<Any>){
        print(dados[0])
        data.removeAll()
        
        if isVoice{
            self.falar(texto: "Escolha uma linha que passa por aqui.")
        }
        var falarLinhas = ""
        
        var dado: MeuOnibusDadosTabela
        for i in 0...dados.count-1 {
            let objAtual = dados[i] as? [String: Any]
            dado = MeuOnibusDadosTabela(numero: objAtual!["numero"] as! String, nome: objAtual!["nome"] as! String, operacao: objAtual!["operacao"] as! Int, sentido: objAtual!["sentido"] as! Int)
            
            data.append(dado)
            if isVoice{
                falarLinhas += "Linha " + (objAtual!["numero"] as! String) + " " + (String(objAtual!["operacao"] as! Int))
                falarLinhas += " " + (objAtual!["nome"] as! String) + ". \n "
            }
        }
        //self.UITableView.reloadData()
        DispatchQueue.main.async {
            let index = IndexPath(item: 0, section: 0)
            let cell = self.UITableView.cellForRow(at: index) as! MeuOnibusCelula
            cell.imgMiniLinha.isHidden = false
            cell.txtLinha.isHidden = false
            self.UITableView.reloadData()
        }
        if isVoice{
            self.falar(texto: self.trocaAbrev(texto: falarLinhas))
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "MeuOnibusCelula") as! MeuOnibusCelula
        let obj = data[indexPath.row]
        
        cell.txtLinha?.text = obj.numero + "-" + String(obj.operacao)
        cell.txtNome?.text = obj.nome
        
        return cell //4.
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        
        //let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "testeV") as! ViewController
        //self.navigationController!.pushViewController(VC1, animated: true)
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
                                        self.buscarLinhas(latlng: String(objetoJson["lat"] as! Double) + "," + String(objetoJson["lng"] as! Double))
                                        if self.isVoice{
                                            self.falar(texto: "Buscando linhas em " + (objetoJson["rua"] as! String) + ", número " + (objetoJson["num"] as! String) + ".")
                                        }
                                    }else{
                                        DispatchQueue.main.async {
                                            self.lblEnd.text = "Endereço não Encontrado"
                                        }
                                        if self.isVoice{
                                            self.falar(texto: "Endereço não Encontrado.")
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
        if segue.identifier == "ProximoMeuOnibus"{
            
            if linhaPorFala >= 0{
                let linha = self.data[linhaPorFala].numero
                let op = self.data[linhaPorFala].operacao
                let sentido = self.data[linhaPorFala].sentido
                let vcDestino = segue.destination as! ProximoMeuOnibusViewController
                vcDestino.linha = linha!
                if let ope = op?.description{
                    vcDestino.operacao = ope
                }
                if let sen = sentido?.description{
                    vcDestino.sentido = sen
                }
                vcDestino.latlng = self.coordenadas
            }else if let indexPath = UITableView.indexPathForSelectedRow{
                let linha = self.data[indexPath.row].numero
                let op = self.data[indexPath.row].operacao
                let sentido = self.data[indexPath.row].sentido
                let vcDestino = segue.destination as! ProximoMeuOnibusViewController
                vcDestino.linha = linha!
                if let ope = op?.description{
                    vcDestino.operacao = ope
                }
                if let sen = sentido?.description{
                    vcDestino.sentido = sen
                }
                vcDestino.latlng = self.coordenadas
            }
        }
    }
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if self.nenhumOnibus  {
            return false
        }
        return true
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    
    @IBAction func btnPesq(_ sender: Any) {
        self.txtLinha.resignFirstResponder()
        if escrevendo{
            let imagem: UIImage = #imageLiteral(resourceName: "editar")
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(MeuOnibus.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
        print(comand.lowercased().range(of: "buscar em") as Any as Any)
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else if comand.lowercased().range(of: "buscar em") != nil{
            let coman = comand.replacingOccurrences(of: "buscar em", with: "")
            print("Buscar em - " + coman)
            if coman.count >= 0{
                self.buscarEnd(end: coman)
            }
        }else if comand.lowercased().range(of: "linha") != nil{
            let coman = comand.replacingOccurrences(of: "linha", with: "")
            print("linha - " + coman)
            if coman.count >= 0{
                self.buscarEmLinhas(linha: coman)
            }
        }
    }
    func buscarEmLinhas(linha: String){
        var dado: MeuOnibusDadosTabela
        var linhaIdentificada = false
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
                let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "ProximoMeuOnibus") as! ProximoMeuOnibusViewController
                
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
                
                linhaIdentificada = true
                
                self.navigationController!.pushViewController(VC1, animated: true)
                break
            }
        }
        if !linhaIdentificada{
            for i in 0...self.data.count-1 {
                dado = data[i]
                let linhaComparar = dado.numero + " " + String(dado.operacao)
                print(linha.westernArabicNumeralsOnly)
                print(linhaComparar.westernArabicNumeralsOnly)
                if linha.westernArabicNumeralsOnly == linhaComparar.westernArabicNumeralsOnly{
                    if linha.lastWord.lowercased() == dado.nome.lastWord.lowercased(){
                        self.linhaPorFala = i
                        let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "ProximoMeuOnibus") as! ProximoMeuOnibusViewController
                        
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
                        
                        linhaIdentificada = true
                        
                        self.navigationController!.pushViewController(VC1, animated: true)
                        break
                    }
                }
            }
        }
        if !linhaIdentificada{
            self.falar(texto: "Linha não identificada.")
        }
    }
    @IBAction func btnOuvirIniciar(_ sender: Any) {
        if status != .recognizing{
            print("Ouvindo")
            startRecording()
            status = .recognizing
            synth.stopSpeaking(at: AVSpeechBoundary.immediate) // Parar fala
            tocaSons(qual: 1)
        }
    }
    
    @IBAction func btnOuvirfinalizar(_ sender: Any) {
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
