import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';
import { Result, Button, Typography, Card } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

const { Paragraph, Text } = Typography;

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  // Called when an error occurs during rendering
  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, errorInfo: null };
  }

  // Called to log the error
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
    this.setState({ errorInfo });
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ 
          height: '100vh', 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          background: '#f0f2f5' 
        }}>
          <Result
            status="500"
            title="An error occurred"
            subTitle="Sorry, the application encountered an unexpected error."
            extra={
              <Button type="primary" icon={<ReloadOutlined />} onClick={this.handleReload}>
                Reload page
              </Button>
            }
          >
            {/* Display error details for debugging (Only show when needed in dev) */}
            {this.state.error && (
              <div className="desc">
                <Paragraph>
                  <Text strong style={{ fontSize: 16 }}>Error details:</Text>
                </Paragraph>
                <Card style={{ width: '100%', maxWidth: 600, overflow: 'auto' }}>
                  <Paragraph type="danger" code>
                    {this.state.error.toString()}
                  </Paragraph>
                  {this.state.errorInfo && (
                    <Paragraph type="secondary" style={{ fontSize: '12px' }}>
                      {this.state.errorInfo.componentStack}
                    </Paragraph>
                  )}
                </Card>
              </div>
            )}
          </Result>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;