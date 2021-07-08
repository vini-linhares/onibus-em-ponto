//
//  PontoViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 02/10/17.
//   
//

import UIKit
import MapKit
import AVFoundation // Falar
import Speech       //Ouvir

class PontoViewController: UIViewController, CLLocationManagerDelegate, AVAudioPlayerDelegate{
    
    
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
    
    @IBOutlet weak var lblEnd: UILabel!
    @IBOutlet weak var lblDestino: UILabel!
    @IBOutlet weak var lblDist: UILabel!
    @IBOutlet weak var txtEnd: UITextField!
    @IBOutlet weak var btnDireita: UIButton!
    
    
    
    var latitude: CLLocationDegrees = 0
    var longitude: CLLocationDegrees = 0
    var latlngDestino: String = ""
    var SwiftTimer = Timer()
    var SwiftCounter = 0
    var calcularDist = false
    var calculando = false
    var distValor = 0
    var distTexto = ""
    
    @objc public func updateCounter() {
        if calcularDist && !calculando{
            calcDist()
        }
    }
    
    
    
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
    
    
    
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        gerenciadorLocalizacao.delegate = self
        gerenciadorLocalizacao.desiredAccuracy = kCLLocationAccuracyBest
        gerenciadorLocalizacao.requestWhenInUseAuthorization()
        gerenciadorLocalizacao.startUpdatingLocation()
        
        SwiftTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(PontoViewController.updateCounter), userInfo: nil, repeats: true)
        
        if getVoice() == "s"{
            //Botão
            let screenSize: CGRect = UIScreen.main.bounds
            btnDireita.frame = CGRect(x: screenSize.width * 0.5, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
            btnDireita.imageEdgeInsets = UIEdgeInsetsMake(btnDireita.frame.size.height, btnDireita.frame.size.width, btnDireita.frame.size.height, btnDireita.frame.size.width)
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
        SwiftTimer.invalidate()
        super.viewDidDisappear(animated)
    }
    
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let localizacaoUsuario = locations.last!
        
        latitude = localizacaoUsuario.coordinate.latitude
        longitude = localizacaoUsuario.coordinate.longitude
        
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
                            self.falar(texto: "Estâmos em " + rua + ", número " + num + ". Diga o seu destino.")
                            self.mensagemInicio = false
                            self.ruaAnterior = rua
                            self.numAnterior = num
                        }
                        
                        if self.destinoIdentificado{
                            if !(self.ruaAnterior == rua) {
                                self.falar(texto: rua + ", " + num)
                                self.ruaAnterior = rua
                                self.numAnterior = num
                            }else{
                                if let numAtual = Int(num){
                                    if let numAnt = Int(self.numAnterior){
                                        let diferenca = numAtual - numAnt
                                        if(diferenca > 500 || diferenca < -500){
                                            self.falar(texto: "Estâmos em " + rua + ", " + num + ". Faltam " + self.distTexto)
                                            self.ruaAnterior = rua
                                            self.numAnterior = num
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            }else{
                print("erro")
            }
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
    
    @IBAction func btnPesq(_ sender: Any) {
        if let linha = txtEnd.text{
            pesquisar(texto: linha)
        }
    }
    func pesquisar(texto: String){
        self.txtEnd.resignFirstResponder()
        
        let linha = texto
            if linha.count >= 3{
                
                //if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/linha/?busca="+linha){
                if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/PegaLatlng/?end="+subs(texto: linha)){
                    let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                        if erro == nil{
                            if let dadosRetorno = dados{
                                //print(dadosRetorno)
                                do{
                                    if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                        //print(objetoJson)
                                        var rua1 = ""
                                        var num1 = ""
                                        if let rua = objetoJson["rua"]{
                                            rua1 = rua as! String
                                            //print(linhas)
                                            if let num = objetoJson["num"]{
                                                num1 = num as! String
                                                DispatchQueue.main.async {
                                                    self.lblDestino.text = (rua as! String) + ", " + (num as! String)
                                                }
                                            }else{
                                                DispatchQueue.main.async {
                                                    self.lblDestino.text = rua as? String
                                                }
                                            }
                                        }
                                        if let lat = objetoJson["lat"]{
                                            if let lng = objetoJson["lng"]{
                                                self.latlngDestino = String(describing: lat) + "," + String(describing: lng)
                                                self.calcularDist = true
                                                self.calcDist()
                                                if self.isVoice{
                                                    print("Aqui Amigo")
                                                    if self.latlngDestino == "0,0"{
                                                        self.falar(texto: "Local não identificado")
                                                        self.destinoIdentificado = false
                                                    }else{
                                                        self.falar(texto: "Calculando distancia até " + rua1 + ", número " + num1)
                                                        self.falarDistancia = true
                                                        self.destinoIdentificado = true
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
            }else{
                if isVoice{
                    self.falar(texto: "Diga um endereço válido")
                }else{
                    let alerta = UIAlertController(title: "Atenção", message: "Digite pelo menos 3 caracteres para relaizar a busca", preferredStyle: .alert)
                    let ok = UIAlertAction(title: "OK", style: .default, handler: nil)
                    alerta.addAction(ok)
                    present(alerta, animated: true, completion: nil)
                }
            }
        
    }
    
    func calcDist(){
        calculando = true
        SwiftCounter += SwiftCounter
        print(SwiftCounter)
        var latlng = self.latlngDestino
        print(latlng)
        if let url = URL(string: "http://virtualartsa.com.br/onibusemponto/api/calcDist/?inicio="+latlng+"&fim="+String(self.latitude)+","+String(self.longitude)){
            let tarefa = URLSession.shared.dataTask(with: url) { (dados, requisicao, erro) in
                if erro == nil{
                    if let dadosRetorno = dados{
                        //print(dadosRetorno)
                        do{
                            if let objetoJson = try JSONSerialization.jsonObject(with: dadosRetorno, options: []) as? [String: Any]{
                                print(objetoJson)
                                if let texto = objetoJson["texto"]{
                                    self.distTexto = texto as! String
                                    DispatchQueue.main.async {
                                        self.lblDist.text = self.distTexto
                                    }
                                    if self.isVoice && self.falarDistancia{
                                        self.falar(texto: "Faltam " + self.distTexto)
                                        self.falarDistancia = false
                                    }
                                }
                                if let valor = objetoJson["valor"]{
                                    self.distValor = valor as! Int
                                }
                                self.calculando = false
                            }
                        }catch{
                            print("Erro no Retorno")
                            self.calculando = false
                        }
                    }
                    
                    
                }else{
                    print("erro")
                }
            }
            tarefa.resume()
        }
    }
    
    
    
    func subs(texto: String) -> String{
        let s1 = texto
        var s2 = s1.lowercased()
        s2 = s2.replacingOccurrences(of: " ", with: "%20")
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(PontoViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else{
            pesquisar(texto: comand)
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
