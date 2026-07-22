library stone_smart_mcpark;

import 'package:flutter/services.dart';

import 'handler/istone_handler.dart';
import 'handler/istone_smart_handler.dart';
import 'payments/payment.dart';

// ── API publica ──────────────────────────────────────────────────────────────
// Antes estes tipos vinham do pacote separado `interface_stone_smart_flutter`,
// publicado no pub.dev por terceiro. Foram ABSORVIDOS aqui: os modelos eram
// usados ate na camada de dominio do app, e depender do pub.dev de um autor que
// migrou para modelo comercial deixava metade da integracao fora do nosso
// controle.

// Types
export 'utils/types/payment_type_call.dart';
export 'utils/types/payment_type_credit.dart';
export 'utils/types/payment_type_handler.dart';
export 'utils/types/payment_type_transaction.dart';
export 'utils/types/printer_type.dart';

// Extensions
export 'utils/extensions/payment_type_call_ext.dart';
export 'utils/extensions/payment_type_credit_ext.dart';
export 'utils/extensions/payment_type_handler_ext.dart';
export 'utils/extensions/payment_type_transaction_ext.dart';
export 'utils/extensions/printer_type_ext.dart';

// Handlers
export 'handler/istone_handler.dart';
export 'handler/istone_smart_handler.dart';

// Models
export 'model/stone_printer.dart';
export 'model/stone_response.dart';
export 'model/stone_transaction_model.dart';

// Payments
export 'payments/payment.dart';

class StoneSmart {
  /// Identificador do MethodChannel — protocolo de fio entre Dart e Java.
  ///
  /// ⚠ NAO RENOMEAR sem alterar `CHANNEL_NAME` no lado Android
  /// (`StoneSmartFlutterPlugin.java`) no MESMO commit. Os dois precisam ser
  /// identicos; divergencia NAO da erro de compilacao — o canal simplesmente
  /// nunca responde, que e um modo de falha silencioso.
  ///
  /// Mantido como `stone_smart_flutter` de proposito na renomeacao para
  /// `stone_smart_mcpark`: e identificador interno, renomea-lo traria risco sem
  /// nenhum ganho funcional.
  static const channelName = "stone_smart_flutter";

  final MethodChannel _channel;
  Payment? _payment;

  static StoneSmart? _instance;

  StoneSmart(this._channel);

  static StoneSmart instance() {
    _instance ??= StoneSmart(const MethodChannel(channelName));
    return _instance!;
  }

  /// Function to initialize payment and register the notification handler
  void initPayment({
    required IStoneHandler handler,
    IStoneSmartHandler? iStoneSmartHandler,
  }) {
    _payment = Payment(
      channel: _channel,
      paymentHandler: handler,
      iStoneSmartHandler: iStoneSmartHandler,
    );
  }

  /// Get the Payment object. Note: It needs to be initialized.
  Payment get payment {
    if (_payment == null) {
      throw "PAYMENT NEEDS TO BE INITIALIZED! \n TRY: StoneSmart._instance.initPayment(handler)";
    }
    return _payment!;
  }
}
