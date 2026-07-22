package br.com.mcpark.stone_smart;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import br.com.mcpark.stone_smart.core.StoneSmart;
import br.com.mcpark.stone_smart.payments.PaymentsFragment;
import br.com.mcpark.stone_smart.payments.PaymentsPresenter;
import br.com.mcpark.stone_smart.payments.PaymentsUseCase;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

public class StoneSmartFlutterPlugin
  implements FlutterPlugin, MethodCallHandler {

  // Identificador do MethodChannel — protocolo de fio entre Java e Dart.
  //
  // ⚠ NAO RENOMEAR sem alterar `StoneSmart.channelName` em
  // lib/stone_smart_mcpark.dart no MESMO commit. Divergencia NAO da erro de
  // compilacao: o canal simplesmente nunca responde (falha silenciosa).
  //
  // Mantido como "stone_smart_flutter" de proposito quando o pacote foi
  // renomeado para stone_smart_mcpark — e identificador interno, renomea-lo
  // traria risco sem ganho.
  private static final String CHANNEL_NAME = "stone_smart_flutter";
  private MethodChannel channel;
  private Context context;
  private StoneSmart stoneSmart;

  public StoneSmartFlutterPlugin() {}

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_NAME);
    //Get context to application
    context = binding.getApplicationContext();
    channel.setMethodCallHandler(this);
    //Create instance to Stone Smart class
    stoneSmart = new StoneSmart(context, channel);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    //Function responsible for listening to methods called by flutter
    if (call.method.startsWith("payment")) {
      stoneSmart.initPayment(call, result);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    //Dispose plugin
    channel.setMethodCallHandler(null);
    channel = null;
    stoneSmart.dispose();
    stoneSmart = null;
  }
}
