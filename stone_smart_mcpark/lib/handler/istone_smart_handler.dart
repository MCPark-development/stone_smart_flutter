import 'package:stone_smart_mcpark/model/stone_response.dart';
import 'package:stone_smart_mcpark/model/stone_transaction_model.dart';

abstract class IStoneSmartHandler {
  void onTransactionSuccess();

  void onError(StoneResponse response);

  void onMessage(String message);

  void onChanged(StoneResponse response);

  void onFinishedResponse(StoneTransactionModel response);

  void onLoading(bool show);

  void writeToFile({
    String? transactionCode,
    String? transactionId,
    String? response,
  });

  void onAbortedSuccessfully();

  void onAuthProgress(StoneResponse response);

  void onTransactionInfo(String response);
}
