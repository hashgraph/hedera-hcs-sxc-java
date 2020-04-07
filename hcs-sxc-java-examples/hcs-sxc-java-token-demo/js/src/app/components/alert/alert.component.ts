import {Component, OnInit, OnDestroy, Inject} from '@angular/core';
import { Subscription } from 'rxjs';
import { RxStompService} from '@stomp/ng2-stompjs';
import {Message} from '@stomp/stompjs';
import {AlertService} from '@/services';
import {Notification, WINDOW} from '@/models';
import {EnvService} from '@/services';

@Component({
  // tslint:disable-next-line:component-selector
    selector: 'alert',
    templateUrl: 'alert.component.html'
})

export class AlertComponent implements OnInit, OnDestroy {
    private subscription: Subscription;
    message: any;
    private topicSubscription: Subscription;
    private stompUrl: string;
    constructor(
      private alertService: AlertService,
      private rxStompService: RxStompService,
      private env: EnvService,
      @Inject(WINDOW) private window: Window
    ) {
      this.stompUrl = 'ws://' + this.window.location.hostname + ':' + env.javaPort + '/notifications/websocket';
    }

    ngOnInit() {
      this.subscription = this.alertService.getMessage().subscribe(message => {
          this.message = message;
      });

      this.rxStompService.stompClient.brokerURL = this.stompUrl;
      this.topicSubscription = this.rxStompService.watch('/notifications/messages').subscribe((message: Message) => {
        this.onReceiveMessage(message.body);
      });
    }

  onReceiveMessage(message: string): void {
    const notification: Notification = JSON.parse(message);
    if (notification.message.startsWith('OK')) {
      this.alertService.success(notification.message.substr('OK'.length), true);
    } else {
      this.alertService.error(notification.message.substr('ERROR'.length), true);
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
    this.topicSubscription.unsubscribe();
  }
}
