# Casa Launcher

Launcher Android TV próprio, com interface clara, relógio/data e atalhos para YouTube, Netflix, Prime Video e Disney+.

## Construir

Com o Android Studio/JDK e Android SDK instalados, rode:

```sh
bash scripts/build-apk.sh
```

O script detecta o SDK em `ANDROID_SDK_ROOT` ou `ANDROID_HOME` (ou `~/Android/Sdk`) e usa a plataforma e as ferramentas de build mais recentes instaladas. O APK assinado localmente fica em `build-local/CasaLauncher.apk`. O build não baixa código de launchers de terceiros.

## Instalar pela rede Wi-Fi

ADB sem fio funciona em Android 11 quando a versão do fabricante expõe a opção **Depuração sem fio**. O computador e a TV precisam estar na mesma rede.

1. Na TV, abra **Configurações > Preferências do dispositivo > Sobre** e pressione **Compilação** sete vezes para habilitar opções do desenvolvedor (os nomes podem variar).
2. Em **Opções do desenvolvedor**, procure **Depuração sem fio**. Ative-a e abra **Parear dispositivo com código de pareamento**.
3. No computador com Android SDK Platform-Tools instalado, use os endereços/portas mostrados pela TV:

   ```sh
   adb pair IP_DA_TV:PORTA_DE_PAREAMENTO
   adb connect IP_DA_TV:PORTA_DE_CONEXAO
   adb install -r build-local/CasaLauncher.apk
   ```

   O comando `adb pair` solicitará o código exibido na TV. Se a opção Depuração sem fio não existir, a instalação poderá exigir ADB por USB ou habilitar depuração de rede por outro método suportado pelo firmware.

4. Defina Casa como tela inicial padrão. Nesta TV, também foi necessário desativar o launcher original por usuário para que o botão Home não o chamasse diretamente.

Para reverter via ADB, reative o launcher original e escolha-o como tela inicial:

```sh
adb shell pm enable --user 0 com.google.android.tvlauncher
adb shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
```

Também é possível abrir as configurações do sistema pelo botão de configurações do controle e desinstalar Casa em **Configurações > Apps**.

## Observações

- O app não acessa internet, contas ou conteúdo dos serviços; apenas abre os apps instalados na TV.
- Se algum serviço não estiver instalado, o cartão correspondente informa isso ao ser selecionado.
- A substituição do botão Home depende do firmware da Philco aceitar apps de terceiros como tela inicial padrão.
