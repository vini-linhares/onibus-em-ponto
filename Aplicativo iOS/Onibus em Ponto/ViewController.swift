//
//  ViewController.swift
//  Onibus em Ponto
//
//  Created by Vinícius Schiavetto on 27/09/17.
//   
//

import UIKit
import AVFoundation // Audio
import Speech       //Ouvir
/*
extension UIView {
    /**
     Set x Position
     
     :param: x CGFloat
     */
    func setX(x:CGFloat) {
        var frame:CGRect = self.frame
        frame.origin.x = x
        self.frame = frame
    }
    /**
     Set y Position
     
     :param: y CGFloat
     */
    func setY(y:CGFloat) {
        var frame:CGRect = self.frame
        frame.origin.y = y
        self.frame = frame
    }
    /**
     Set Width
     
     :param: width CGFloat
     */
    func setWidth(width:CGFloat) {
        var frame:CGRect = self.frame
        frame.size.width = width
        self.frame = frame
    }
    /**
     Set Height
     
     :param: height CGFloat
     */
    func setHeight(height:CGFloat) {
        var frame:CGRect = self.frame
        frame.size.height = height
        self.frame = frame
    }
}
*/
class ViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, AVAudioPlayerDelegate{
    
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
    
    
    
    @IBOutlet weak var UITableView: UITableView!
    @IBOutlet weak var btnDireita: UIButton!
    
    
    var menus: [Menu] = []
    
    var tutorialVisto = "tutorialVisto"
    
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
        
        UITableView.dataSource = self
        UITableView.delegate = self
        
        var menu: Menu
        menu = Menu(imagem: #imageLiteral(resourceName: "btn1"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn3"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn4"))
        menus.append(menu)
        menu = Menu(imagem: #imageLiteral(resourceName: "btn5"))
        menus.append(menu)
        
        
        //TutorialViewController
        if recuperarNota() == "n" {
            UserDefaults.standard.set("s", forKey: tutorialVisto)
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "TutorialViewController") as! TutorialViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
        
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
    
    func ativarVoz(){
        self.btnDireita.isHidden = false
        self.isVoice = true
        self.setVoice(voz: true)
        let screenSize: CGRect = UIScreen.main.bounds
        self.btnDireita.frame = CGRect(x: screenSize.width * 0.5, y: 0, width: screenSize.width * 0.5, height: screenSize.height)
        self.btnDireita.imageEdgeInsets = UIEdgeInsetsMake(self.btnDireita.frame.size.height, self.btnDireita.frame.size.width, self.btnDireita.frame.size.height, btnDireita.frame.size.width)
    }
    
    func desativarVoz(){
        self.btnDireita.isHidden = true
        self.isVoice = false
        self.setVoice(voz: false)
    }
    
    func getVoice() -> String{
        if let voiceSalva = UserDefaults.standard.object(forKey: salvaVoice){
            return voiceSalva as! String
        }
        return "n"
    }
    
    func setVoice(voz: Bool){
        if(voz){
            UserDefaults.standard.set("s", forKey: salvaVoice)
        }else{
            UserDefaults.standard.set("f", forKey: salvaVoice)
        }
    }
    
    func recuperarNota() -> String{
        if let notaSalva = UserDefaults.standard.object(forKey: tutorialVisto){
            return notaSalva as! String
        }
        return "n"
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return menus.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let menu: Menu = menus[indexPath.row]
        let celulaReuso = "homeCelulaReuso"
        let celula = tableView.dequeueReusableCell(withIdentifier: celulaReuso, for: indexPath) as! HomeCelula
        
        //celula.imageView?.image = menu.imagem
        
        celula.homeImageView.image = menu.imagem
        celula.sizeToFit()
        
        //celula.homeImageView.center = celula.center;
        
        //let screenSize: CGRect = UIScreen.main.bounds
        //var h = tableView.rowHeight
        //celula.homeImageView.frame = CGRect(x: screenSize.width, y: 0, width: screenSize.width, height: h)
        
        //celula.homeImageView.setHeight(height: 150)
        //celula.homeImageView.setWidth(width: 444)
        
        return celula
    }
    /*
    func imageResize (image:UIImage, sizeChange:CGSize)-> UIImage{
        
        let hasAlpha = true
        let scale: CGFloat = 0.0 // Use scale factor of main screen
        
        UIGraphicsBeginImageContextWithOptions(sizeChange, !hasAlpha, scale)
        image.draw(in: CGRect(origin: CGPointZero, size: sizeChange))
        
        let scaledImage = UIGraphicsGetImageFromCurrentImageContext()
        return scaledImage!
    }
    */
    
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print(indexPath.row)
        tableView.deselectRow(at: indexPath as IndexPath, animated: true)
        if indexPath.row == 0{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "SelecionarOnibusVIewController") as! SelecionarOnibusVIewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
        if indexPath.row == 1{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "PontoViewController") as! PontoViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
        if indexPath.row == 2{
            //-self.performSegue(withIdentifier: "OndeEstouPassandoViewController", sender:self)
            //--let viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "OndeEstouPassandoViewController")
            //--self.present(viewController, animated: false, completion: nil)
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "OndeEstouPassandoViewController") as! MeuLocalViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
        if indexPath.row == 3{
            //self.performSegue(withIdentifier: "OndeEstouPassandoViewController", sender:self)
            //let viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "TutorialViewController")
            //self.present(viewController, animated: false, completion: nil)
            
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "DetalhesVIewController") as! DetalhesVIewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func btnConfig(_ sender: Any) {
        let alerta = UIAlertController(title: "Comandos de Voz", message: "Deseja ativar os recursos de voz?", preferredStyle: .alert)
        let confirmar = UIAlertAction(title: "Ativar", style: .default) { (acao) in
            self.ativarVoz()
        }
        let cancelar = UIAlertAction(title: "Desativar", style: .cancel){ (acao) in
            self.desativarVoz()
        }
        alerta.addAction(confirmar)
        alerta.addAction(cancelar)
        present(alerta, animated: true, completion: nil)
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
        speechTimer = Timer.scheduledTimer(timeInterval: 2, target:self, selector: #selector(ViewController.updateCounterSpeech), userInfo: nil, repeats: true)
        
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
        if comand == "voltar"{
            if let navigationController = self.navigationController
            {
                navigationController.popViewController(animated: true)
            }
        }else if comand == "onde está meu ônibus" || comand == "onde está o meu ônibus"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "SelecionarOnibusVIewController") as! SelecionarOnibusVIewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "qual o próximo ônibus" || comand == "qual é o próximo ônibus"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "SelecionarOnibusVIewController") as! SelecionarOnibusVIewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "avise meu ponto" || comand == "avise meu." || comand == "avise meu ." || comand == "a vice meu." || comand == "avise 1000 ." || comand == "avise 1000." || comand == "avise-me."{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "PontoViewController") as! PontoViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "onde estou passando"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "OndeEstouPassandoViewController") as! MeuLocalViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "detalhes da linha" || comand == "detalhe da linha"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "DetalhesVIewController") as! DetalhesVIewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "tutorial"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "TutorialViewController") as! TutorialViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "termos de uso"{
            let VC1 = self.storyboard!.instantiateViewController(withIdentifier: "TermosViewController") as! TermosViewController
            self.navigationController!.pushViewController(VC1, animated: true)
        }else if comand == "ativar comandos de voz" || comand == "ativar comando de voz"{
            self.ativarVoz()
        }else if comand == "desativar comandos de voz" || comand == "desativar comando de voz"{
            self.desativarVoz()
        }else {
            self.falar(texto: "Comando não Identificado")
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
